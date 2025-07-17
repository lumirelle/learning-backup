clear
close all
clc
%% 分段线性变换

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

grayAdjust = imadjust(gray, [0.2 0.8], [0 1]);
figure,imshow(grayAdjust),title("（2）分段线性变换")