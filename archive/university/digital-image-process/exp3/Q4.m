clear
close all
clc
%% 计算各区域边界点的傅里叶描绘子并用四分之一点重建边界

image=imread("plane.JPG");
gray=rgb2gray(image);
bw=imbinarize(gray);

se=strel('square',3);
morph=imopen(bw,se);
morph=imclose(morph,se);

boundaries=bwboundaries(1-morph);
M=zeros(length(boundaries));
for k=1:length(boundaries)
    N=length(boundaries{k});
    if N/2~=round(N/2)
        boundaries{k}(end+1,:)=boundaries{k}(end,:);
        N=N+1;
    end
    M(k)=N*3/4;
end

background=zeros(size(morph));
figure,imshow(background),title("（4）重建边界");
hold on;
for k=1:length(boundaries)
    z=boundaries{k}(:,2)+1i*boundaries{k}(:,1);
    Z=fft(z);
    [Y,I]=sort(abs(Z));
    for count=1:M(k)
        Z(I(count))=0;
    end
    zz=ifft(Z);
    plot(real(zz),imag(zz),'w');
end