clear
close all
clc
%% 灰度变换法的伪彩色增强

image1 = imread("lotus.bmp");
gray = rgb2gray(image1);

gray = double(gray); % 转为 double 值，方便后面进行除法运算
[M,N] = size(gray);

% 进行彩色映射
grayImproved = ones(M,N,3);
L = 256;
for i = 1:M
    for j = 1:N
        if gray(i,j) <= L / 4
            grayImproved(i,j,1) = 0;
            grayImproved(i,j,2) = 4 * gray(i,j);
            grayImproved(i,j,3) = L;
        elseif gray(i,j) <= L / 2
            grayImproved(i,j,1) = 0;
            grayImproved(i,j,2) = L;
            grayImproved(i,j,3) = -4 * gray(i,j) + 2 * L;
        elseif gray(i,j) <= 3 * L / 4
            grayImproved(i,j,1) = 4 * gray(i,j) - 2 * L;
            grayImproved(i,j,2) = L;
            grayImproved(i,j,3) = 0;
        else
            grayImproved(i,j,1) = L;
            grayImproved(i,j,2) = -4 * gray(i,j) + 4 * L;
            grayImproved(i,j,3) = 0;
        end
    end
end

grayImproved = uint8(grayImproved); % 转回 uint8 类型
figure,imshow(grayImproved),title("（4）伪彩色增强")
