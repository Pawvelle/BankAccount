@echo off
chcp 65001 >nul
echo 正在编译项目源码...
javac -encoding UTF-8 -d bin -sourcepath src/main/java src/main/java/com/bank/ui/Main.java
if %errorlevel% equ 0 (
    echo 编译成功，正在启动系统...
    java -cp bin com.bank.ui.Main
) else (
    echo 编译失败，请检查错误。
    pause
)
