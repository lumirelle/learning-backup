clear
close all
clc
%% 直方图均衡化

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

grayHisteq = histeq(gray);
figure,imshow(grayHisteq),title("（3）直方图均衡化")