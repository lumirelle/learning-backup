clear
close all
clc
%% 灰度化并二值化

image=imread("plane.JPG");
gray=rgb2gray(image);
bw=imbinarize(gray);
figure,imshow(bw),title("（1）二值化图像")