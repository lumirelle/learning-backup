clear
close all
clc
%% 边缘跟踪，用红色线标注

image=imread("plane.JPG");
gray=rgb2gray(image);
bw=imbinarize(gray);

se=strel('square',3);
morph=imopen(bw,se); 
morph=imclose(morph,se);
 
[boundaries,labels]=bwboundaries(1-morph);
figure,imshow(labels),title('（3）划分的区域');
hold on;
for i=1:length(boundaries)
    boundary=boundaries{i};
    plot(boundary(:,2),boundary(:,1),'r','LineWidth',2);
end