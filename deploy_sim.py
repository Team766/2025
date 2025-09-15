#!/usr/bin/env python3

import os
import shutil
import socket
import subprocess
import time

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

    print("Waiting for robot code to start")
    connected = False
    iter = 0
    while not connected:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            try:
                s.connect(("localhost", 5800))
                connected = True
            except ConnectionRefusedError:
                iter += 1
                print("Waiting for robot code to start" + "." * iter, end="\r")
                time.sleep(5)
    print()

    os.execl("./run.sh", "./run.sh", project_base, f"{project_base}/build-sim")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass