#!/usr/bin/env python3

import os
import shutil
import socket
import subprocess
import time

def main(argv=None):
    # Handle the case where somebody tried to run this script from somewhere besides the root
    # directory of the repo.
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    project_base = os.getcwd()

    # Install dependencies
    subprocess.check_call(["sudo", "apt", "update"])
    subprocess.check_call(["sudo", "apt", "install", "-y", "screen"])

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

    os.chdir(extracted_dir)

    # If you change this print, also update the regex in .vscode/tasks.json
    print("Waiting for robot code to start")

    # Wait until the robot code has started before starting the simulator.
    # It determines whether the robot code is running by polling whether it can connect to the HTTP
    # server on port 5800 that's built into the robot code.
    # This is because the simulator's run script prints the link to the viewer webpage, but we don't
    # want the user opening that until the robot code has actually started running (otherwise, the
    # right-side pane that tries to display data from the HTTP server built into the robot code will
    # fail to load).
    iter = 0
    while True:
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect(("localhost", 5800))
                break
        except ConnectionRefusedError:
            # Display a simple animation to show signs of life while we're waiting for the
            # robot code to start.
            iter += 1
            print("." * (iter % 6) + "      ", end="\r")
            time.sleep(1)

    # Invoke the simulator package's entrypoint.
    # NOTE(2025-09-19): There appears to be some issue in Codespaces right now that causes
    # the Run Simulator task to randomly restart (sometimes accompanied by the message
    # "Extension Host Process exited with code: null, signal: SIGTERM"). Thus, we run the simulator
    # in a GNU Screen session so that if the task restarts, we can just reconnect to the existing
    # instance of the simulator without any disruption to the user.
    os.execlp(
        "screen", "screen", "-D", "-R", "-S", "simulator",
        "./run.sh", project_base)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass