clear
close all
clc
%% 图像灰度化

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);
figure,imhist(gray),axis tight,title("（1）灰度直方图")