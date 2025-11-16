clear
close all
clc
%% 1. 目标与背景的分割与提取1
% 主要要求：提取红苹果

image=im2double(imread('fruit.jpg'));
figure,imshow(image),title("（1）原图")

% 3x3 参数 0.6 高斯滤波
R=imfilter(image(:,:,1),fspecial('gaussian',[3,3],0.6),'conv');
G=imfilter(image(:,:,2),fspecial('gaussian',[3,3],0.6),'conv');
B=imfilter(image(:,:,3),fspecial('gaussian',[3,3],0.6),'conv');
image=cat(3,R,G,B);
figure,imshow(image),title("（2）高斯滤波")

[M,N]=size(image);
hsv=rgb2hsv(image);
h=hsv(:,:,1);
h(h>330/360)=0; % 接近360°的色调认为是0°
training=h(:); % 获取训练数据
startdata=[0;60/360;120/360;180/360;240/360;300/360]; % 初始点数据。注：设为列向量，方便后续处理
[IDX,C]=kmeans(training,6,'start',startdata);
idbw=(IDX==1); % 取边缘
template=reshape(idbw,size(h)); % 二值化图像
figure,imshow(template),title('（3）k 均值聚类分割');

R=image(:,:,1);
G=image(:,:,2);
B=image(:,:,3);
Template=rem(template+1,2); % 将模板中 1 变成 0，0 变成 1，从而将背景变成白色，也不影响目标的提取
R1=R.*template+Template;
G1=G.*template+Template;
B1=B.*template+Template;
templateR=cat(3,R1,G1,B1);
figure,imshow(templateR),title('（4）目标提取');



