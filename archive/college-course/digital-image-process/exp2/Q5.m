clear
close all
clc
%% 添加噪声并平滑

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

% 添加噪声
grayNoiseG = imnoise(gray, "gaussian");
grayNoiseSP = imnoise(gray, "salt & pepper");

% 平滑噪声
grayNGFilt = medfilt2(grayNoiseG);
grayNSPFilt = medfilt2(grayNoiseSP);
figure,imshow(grayNGFilt),title("（5）高斯噪声平滑")
figure,imshow(grayNSPFilt),title("（6）椒盐噪声平滑")