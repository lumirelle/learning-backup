% 旋转、缩放并展示
% 使用 gray

%% 旋转
grayRotate1=imrotate(gray,50,"nearest");
grayRotate2=imrotate(gray,50,"bilinear");
figure(),imshow(grayRotate1),title("最邻近插值旋转")
figure(),imshow(grayRotate2),title("双线性插值旋转")

%% 缩放
grayResize1=imresize(gray,1.5,"nearest");
grayResize2=imresize(gray,1.5,"bilinear");
figure(),imshow(grayResize1),title("最邻近插值缩放")
figure(),imshow(grayResize2),title("双线性插值缩放")