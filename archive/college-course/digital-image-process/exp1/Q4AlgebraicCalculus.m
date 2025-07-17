% 代数运算并展示
% 使用 image1, image2

%% 调整大小
image2Resize=imresize(image2,[256 256]);

%% 进行代数运算
imageAdd=imadd(image1,image2Resize); % 相加
imageSub=imsubtract(image1,image2Resize); % 相减
imageMul=immultiply(image1,image2Resize); % 相乘
imageDiv=imdivide(image1,image2Resize); % 相除
imageJoi=[image1,image2Resize]; % 拼接
figure(),imshow(imageAdd),title("相加")
figure(),imshow(imageSub),title("相减")
figure(),imshow(imageMul),title("相乘")
figure(),imshow(imageDiv),title("相除")
figure(),imshow(imageJoi),title("拼接")
