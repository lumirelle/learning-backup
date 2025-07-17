% 双线性插值放大图片并展示
% 使用 image1

%% 获取图片大小、颜色通道数
level = 1.5; % 放大倍数

[rows, cols, colors] = size(image1);
rowsNew = round(rows * level);
colsNew = round(cols * level);
image1Resize = uint8(zeros(rowsNew, colsNew, colors));

%% 给原图的四条边加 1 个像素的边界，像素值取自四边
image1Bound = zeros(rows+2, cols+2, colors);
image1Bound(1, 2:cols+1, :) = image1(1, :, :); % 上面
image1Bound(rows+2, 2:cols+1, :) = image1(rows, :, :);% 下面
image1Bound(2:rows+1, 2:cols+1, :) = image1; % 将原图复制到中间
image1Bound(:, 1, :) = image1Bound(:, 2, :); % 左边
image1Bound(:, cols+2, :) = image1Bound(:, cols+1, :); % 右边

%% 计算新图像
for i = 1 : rowsNew
    for j = 1 : colsNew
        X = ( i + 0.5 ) * cols / colsNew - 0.5;
        Y = ( j + 0.5 ) * rows / rowsNew - 0.5;
        
        % 找到Q22点的坐标
        QRRX = ceil(X);
        QRRY = ceil(Y);
        if QRRX == 1
            QRRX = 2;
        end
        if QRRY == 1
            QRRY = 2;
        end

        % 找到Q11点的坐标
        QLLX = QRRX - 1;
        QLLY = QRRY - 1;

        % 找到Q21点的坐标
        QRLX = QRRX - 1;
        QELY = QRRY;

        % 找到Q12点的坐标
        QLRX = QRRX;
        QLRY = QRRY - 1;

        % 根据公式计算该点像素
        newX = [QRRX - X , 1 - QRRX + X];
        newY = [QRRY - Y ; 1 - QRRY + Y];
        for k = 1:3
            Q = [image1Bound(QLLX, QLLY, k), image1Bound(QRLX, QELY, k); image1Bound(QLRX, QLRY, k), image1Bound(QRRX, QRRY, k)];
            image1Resize(i, j, k) =  newX * Q * newY;
        end
    end
end

figure(),imshow(image1Resize),title('缩放后的图像')
 