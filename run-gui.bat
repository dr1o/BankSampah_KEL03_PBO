@echo off
cd /d "%~dp0"
if not exist out mkdir out
echo Mengkompilasi Sistem Bank Sampah (Java OOP + GUI + Neon DB)...
javac -encoding UTF-8 -Xlint:all -cp "lib/*" -d out src/banksampah/*.java src/banksampah/pengguna/*.java src/banksampah/sampah/*.java src/banksampah/setoran/*.java src/banksampah/keuangan/*.java src/banksampah/hadiah/*.java src/banksampah/db/*.java src/banksampah/ui/*.java
if errorlevel 1 (
    echo Kompilasi gagal!
    pause
    exit /b 1
)
echo Menjalankan GUI Bank Sampah...
java --enable-native-access=ALL-UNNAMED -cp "out;lib/*" banksampah.ui.GuiMain
