#!/usr/bin/env python3

import ctypes
import ctypes.util
import os
import shutil
import signal
import socket
import subprocess
import threading
import time

# Constant taken from http://linux.die.net/include/linux/prctl.h
PR_SET_PDEATHSIG = 1

class PrCtlError(Exception):
    pass

def set_parent_exit_signal():
    """
    This uses a Linux-specific feature to configure the child process to receive a SIGTERM signal
    when the parent process (this script) dies. This ensures we don't leave any orphaned processes.
    """
    # http://linux.die.net/man/2/prctl
    libc = ctypes.CDLL(ctypes.util.find_library("c"), use_errno=True)
    if libc.prctl(PR_SET_PDEATHSIG, signal.SIGTERM) != 0:
        errno = ctypes.get_errno()
        raise OSError(errno, f"SET_PDEATHSIG prctl failed: {os.strerror(errno)}")

class TerminatingPopen(subprocess.Popen):
    def __exit__(self, exc_type, value, traceback) -> None:
        self.terminate()
        return super().__exit__(exc_type, value, traceback)

connected = None

def code_monitor_thread():
    """
    This is run in a daemon thread to monitor and report the state of the robot code to VS Code.
    It determines whether the robot code is running by polling whether it can connect to the HTTP
    server on port 5800 that's built into the robot code.
    """
    global connected
    iter = 0
    while True:
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect(("localhost", 5800))
                if connected != True:
                    # If you change this print, also update the regex in .vscode/tasks.json
                    print("Robot code started")
                    connected = True
                iter = 0
        except ConnectionRefusedError:
            if connected != False:
                # If you change this print, also update the regex in .vscode/tasks.json
                print("Waiting for robot code to start")
                connected = False
            # Display a simple animation to show signs of life while we're waiting for the
            # robot code to start.
            iter += 1
            print("." * (iter % 6) + "      ", end="\r")
        time.sleep(1)

def main(argv=None):
    # Handle the case where somebody tried to run this script from somewhere besides the root
    # directory of the repo.
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    project_base = os.getcwd()

    # Create a directory in which to keep all of the simulator's files.
    os.makedirs("build-sim", exist_ok=True)
    os.chdir("build-sim")

    # Download the simulator package from github.
    # The -N flag to wget skips the download if we already have the most recent version (as
    # determined by comparing the modified timestamp of the file to the Last-Modified header
    # returned by the server).
    sim_package = "sim.tar.gz"
    try:
        t1 = os.path.getmtime(sim_package)
    except OSError:
        t1 = None
    subprocess.check_call([
        "wget",
        "-N",
        f"https://github.com/Team766/2020Sim/releases/latest/download/{sim_package}",
    ])
    t2 = os.path.getmtime(sim_package)

    # Extract the simulator package (unless we've already extracted the most recent version).
    extracted_dir = "files"
    if t1 != t2 or not os.path.isdir(extracted_dir):
        shutil.rmtree(extracted_dir, ignore_errors=True)
        os.makedirs(extracted_dir, exist_ok=True)
        subprocess.check_call([
            "tar",
            f"--directory={extracted_dir}",
            "-xf",
            sim_package,
        ])
    
    # This is a backwards compatibility shim for existing 3d simulator packages.
    # Previously, the run script of the simulator was also responsible for running the robot code,
    # so give it some placeholder Java code to run. The simulator has a hard-coded Java class that
    # it expects to run.
    os.makedirs("com/team766/hal/simulator", exist_ok=True)
    with open("com/team766/hal/simulator/RobotMain.java", "w") as fd:
        fd.write(r"""
                 package com.team766.hal.simulator;
                 public class RobotMain {
                     public static void main(final String[] args) {
                         while (true) {
                             try {
                                 Thread.sleep(10000);
                             } catch (InterruptedException e) {
                             }
                         }
                     }
                 }
                 """)
    subprocess.check_call(["javac", "com/team766/hal/simulator/RobotMain.java"])

    os.chdir(extracted_dir)

    # Wait until the robot code has started before starting the simulator.
    # This is because the simulator's run script prints the link to the viewer webpage, but we don't
    # want the user opening that until the robot code has actually started running (otherwise, the
    # right-side pane that tries to display data from the HTTP server built into the robot code will
    # fail to load).
    threading.Thread(target=code_monitor_thread, daemon=True).start()

    while not connected:
        time.sleep(1)

    # Invoke the simulator package's entrypoint.
    with TerminatingPopen(["./run.sh", project_base, f"{project_base}/build-sim"]) as p:
        p.wait()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass