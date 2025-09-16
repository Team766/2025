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
    Return a function to be run in a child process which will trigger SIGNAME
    to be sent when the parent process dies
    """
    # http://linux.die.net/man/2/prctl
    libc = ctypes.CDLL(ctypes.util.find_library("c"), use_errno=True)
    if libc.prctl(PR_SET_PDEATHSIG, signal.SIGABRT) != 0:
        errno = ctypes.get_errno()
        raise OSError(errno, f"SET_PDEATHSIG prctl failed: {os.strerror(errno)}")

class TerminatingPopen(subprocess.Popen):
    def __exit__(self, exc_type, value, traceback) -> None:
        self.terminate()
        return super().__exit__(exc_type, value, traceback)

connected = None

def code_monitor_thread():
    global connected
    iter = 0
    while True:
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect(("localhost", 5800))
                if connected != True:
                    print("Robot code started")
                    connected = True
                iter = 0
        except ConnectionRefusedError:
            if connected != False:
                print("Waiting for robot code to start")
                connected = False
            iter += 1
            print("." * (iter % 6) + "      ", end="\r")
        time.sleep(1)

def main(argv=None):
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    project_base = os.getcwd()

    os.makedirs("build-sim", exist_ok=True)
    os.chdir("build-sim")

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

    threading.Thread(target=code_monitor_thread, daemon=True).start()

    while not connected:
        time.sleep(1)

    with TerminatingPopen(["./run.sh", project_base, f"{project_base}/build-sim"]) as p:
        p.wait()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass