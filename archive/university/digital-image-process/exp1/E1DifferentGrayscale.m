% 用不同的方法灰度化图片并展示
% 使用 image1

%% 转为浮点型矩阵
image1Double = double(image1);

%% 1
imageGray1 = rgb2gray(image1); % 用函数进行RGB到灰度图像的转换  
figure(),imshow(imageGray1),title('函数灰度图像')

%% 2
imageGray3 = (image1Double(:,:,3)+image1Double(:,:,2)+image1Double(:,:,1))/3; % 平均值法转化之后的灰度图像  
imageGray3 = uint8(imageGray3);
figure(),imshow(imageGray3),title('平均值图像')

%% 3
ImageGray2=max(max(image1Double(:,:,1),image1Double(:,:,2)),image1Double(:,:,3)); % 最大值法转化之后的灰度图像  
ImageGray2 = uint8(ImageGray2);
figure(),imshow(ImageGray2),title('最大值图像')
