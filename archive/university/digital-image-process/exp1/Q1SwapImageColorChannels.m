% 交换红绿通道并展示
% 使用 image1
imageSwap=image1;
imageSwap(:,:,1)=image1(:,:,2);
imageSwap(:,:,2)=image1(:,:,1);
figure(),imshow(imageSwap),title('红绿色彩通道互换')