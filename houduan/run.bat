@echo off
echo ===============================================
echo    Spring Boot 心理健康管理系统启动脚本
echo ===============================================
echo.

echo 正在检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装JDK 17或更高版本
    pause
    exit /b 1
)

echo.
echo 正在检查Maven环境...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请确保已安装Maven 3.6或更高版本
    pause
    exit /b 1
)

echo.
echo 正在编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo 正在启动Spring Boot应用...
echo 项目将在端口8080启动
echo 启动完成后可以访问: http://localhost:8080
echo.
echo 按Ctrl+C停止应用
echo.

mvn spring-boot:run

