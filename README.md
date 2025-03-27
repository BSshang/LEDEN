
![Screenshot_2025 0![123](https://github.com/user-attachments/assets/a6b684a9-bcac-4499-96cd-20e3148a2e53)
3 20_14 09 21 522](https://github.com/user-attachments/assets/98dc6867-23fb-48b4-8735-de31a609b5b3)

这是一个android设备通过socket 连接雷丹RFID打印机demo,基础指令使用的APL指令集，兼容板卡和手机，主要完成图片的打印、打印机状态获取、打印剩余张数的获取和青春缓存数据指令
其中主要解决一个打印图片速度非常慢的问题，由于板卡设备受到运行内存的影响，将bitmap数据转换成16进制字节码的时候速度问题影响打印效率；
注意： 打印图片的时候确保打印的图片大小不能超过标签大小，在部分设备上转换完成的数据无法在打印机上运行打印，
