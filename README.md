# PixelCrop
A Crop library like Google Photos.

一个Google Photos风格的图片剪裁库，我特别喜欢的剪裁效果。其中那个角度盘也是自定义的一个控件，自我感觉也模仿的挺像的。

对图片的剪裁处理用了uCrop的so文件，使用了CImg这个库对图片进行剪裁。

### 效果

![](https://github.com/wuapnjie/PixelCrop/blob/master/screenshots/pixelcrop.gif)


### 使用

在`layout.xml`中

```xml
<com.xiaopo.flying.pixelcrop.PixelCropView
        android:id="@+id/pixel_crop_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

其中还内置了一个角度选择器

```xml
 <com.xiaopo.flying.pixelcrop.DegreeSeekBar
     android:id="@+id/seek_bar"
     android:layout_width="180dp"
     android:layout_height="50dp"/>
```

提供的接口

```java
mPixelCropView.rotate(currentDegrees);
```



### 项目说明

这个项目是某天晚上看到google photos的剪裁效果后想做的，因为特别喜欢那个效果。也是我的Matrix三部曲的最后一部曲，另外两部分别为

1. 贴图 [StickerView](https://github.com/wuapnjie/StickerView)
2. 拼图 [PuzzleView](https://github.com/wuapnjie/PuzzleView)

   

这三个项目都是我在学习了Android的Matrix类后思考制作的，代码风格比较相似，个人最喜欢拼图项目，因为这个项目是完全自己设计并实现的。想要学习的朋友可以都看一下。

### License

```
Copyright 2016 wuapnjie

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
