clear
close all
clc
%% 拓展内容

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

% 对以上处理变换参数，查看处理效果
grayAdjust = imadjust(gray, [0.4 0.6], [0 1]);
figure,imshow(grayAdjust),title("（9）分段线性变换——变换参数")

grayHisteq = histeq(gray,128);
figure,imshow(grayHisteq),title("（10）直方图均衡化——变换参数")

grayNoiseG = imnoise(gray, "gaussian", 0.15);
grayNoiseSP = imnoise(gray, "salt & pepper", 0.05);
grayNGFilt = medfilt2(grayNoiseG);
grayNSPFilt = medfilt2(grayNoiseSP);
figure,imshow(grayNGFilt),title("（11）高斯噪声平滑——变换参数")
figure,imshow(grayNSPFilt),title("（12）椒盐噪声平滑——变换参数")

% 设计不同的平滑滤波、锐化滤波方法，查看处理效果
grayDouble = im2double(gray);
grayNoiseG = imnoise(grayDouble,'gaussian');

result1=filter2(fspecial('average',3),grayNoiseG); 
figure,imshow(result1),title('（13）高斯噪声 3*3 均值滤波');

result2 = imfilter(grayNoiseG,fspecial('gaussian',[2*3+1 2*3+1],0.6),'conv');
figure,imshow(result2),title('（14）高斯噪声 sigmal = 0.6 高斯滤波');

result3 = medfilt2(grayNoiseG); 
figure,imshow(result3),title('（15）高斯噪声 3*3 中值滤波'); 

H1=[1 0;0 -1];
H2=[0 1;-1 0];
R1=imfilter(gray,H1);
R2=imfilter(gray,H2);
edgeImage=abs(R1)+abs(R2);
sharpImage=gray+edgeImage;
figure,imshow(sharpImage),title('（16）Roberts 锐化图像');

H1=[-1 -1 -1;0 0 0;1 1 1];
H2=[-1 0 1;-1 0 1;-1 0 1];
R1=imfilter(gray,H1);
R2=imfilter(gray,H2);
edgeImage=abs(R1)+abs(R2);
sharpImage=gray+edgeImage;
figure,imshow(sharpImage),title('（17）Prewitt 锐化图像');

H = fspecial('laplacian',0);
R = imfilter(gray,H);
H1 = [0 -1 0;-1 5 -1;0 -1 0]; 
sharpImage = imfilter(gray,H1);
figure,imshow(sharpImage),title('（18）Laplacian 锐化图像');

% 自行设计方法，实现对彩色图像增强处理
% 将RGB格式的图像转为HSV或者HSI格式，对于亮度值进行修改
HSV = rgb2hsv(image1);
V = HSV(:,:,3);
V = histeq(V);
HSV(:,:,3) = V;
image1Improved = hsv2rgb(HSV);
figure,imshow(image1Improved),title('（19）彩色图像增强处理');
