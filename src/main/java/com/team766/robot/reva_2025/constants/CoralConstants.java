package com.team766.robot.reva_2025.constants;
public class CoralConstants{
    public enum CoralConstant{

        A_R(0,0),
        B_R(0,0),
        C_R(0,0),
        D_R(0,0),
        E_R(0,0),
        F_R(0,0),
        G_R(0,0),
        H_R(0,0),
        I_R(0,0),
        J_R(0,0),
        K_R(0,0),
        L_R(0,0),

        A_B(0,0),
        B_B(0,0),
        C_B(0,0),
        D_B(0,0),
        E_B(0,0),
        F_B(0,0),
        G_B(0,0),
        H_B(0,0),
        I_B(0,0),
        J_B(0,0),
        K_B(0,0),
        L_B(0,0);



        public final double X;
        public final double Z;

        private CoralConstant(double Xa, double Za){
            X = Xa;
            Z = Za;
        }

        public double getX(){
            return X;
        }

        public double getZ(){
            return Z;
        }


    }
}
