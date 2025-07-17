clear
close all
clc
%% 形态学滤波

image=imread("plane.JPG");
gray=rgb2gray(image);
bw=imbinarize(gray);

se=strel('square',3);
morph=imopen(bw,se); 
morph=imclose(morph,se);
figure,imshow(morph),title('（2）形态学滤波');