close all
clear
clc

%% 准备环境
image1 = imread("peppers.jpg");
image2 = imread("lotus.bmp");

%% 普通题
Q1SwapImageColorChannels
Q2GrayscaleImage % gray
Q3RotateResize
Q4AlgebraicCalculus

%% 拓展题
E1DifferentGrayscale
E2ColorSpaceTransform
E3BilinearResize