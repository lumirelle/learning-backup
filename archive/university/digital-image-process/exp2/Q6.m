clear
close all
clc
%% 利用 Sobel 算子锐化

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

H1 = [-1 -2 -1; 0 0 0; 1 2 1];
H2 = [-1 0 1; -2 0 2; -1 0 1];

R1 = imfilter(gray, H1);
R2 = imfilter(gray, H2);

edgeImage = abs(R1) + abs(R2);
sharpImage = gray + edgeImage;
figure,imshow(edgeImage),title("（7）Sobel 梯度图像");
figure,imshow(sharpImage),title("（8）Sobel 锐化图像");