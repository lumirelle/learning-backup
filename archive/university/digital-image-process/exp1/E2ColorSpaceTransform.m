% 把 rgb 空间图像变换到 YCrBr, HSV
% 使用 image1

%% 使用函数转换
image1YCbCr = rgb2ycbcr(image1);
image1HSV = rgb2hsv(image1);

figure()
subplot(1,2,1),imshow(image1HSV),title("HSV空间图像")
subplot(1,2,2),imshow(image1YCbCr),title("YCbCr空间图像")
