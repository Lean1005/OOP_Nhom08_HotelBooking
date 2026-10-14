import os
import re
import shutil

# Thư mục gốc của project hiện tại
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.join(BASE_DIR, "src")
BACKUP_FILE = os.path.join(BASE_DIR, "HotelBookingApp_BanGop.txt")

# Tự động tìm file HotelBookingApp.java ở mọi vị trí có thể trên máy của bạn
CANDIDATE_FILES = [
    os.path.join(SRC_DIR, "HotelBookingApp.java"),
    os.path.join(BASE_DIR, "HotelBookingApp.java"),
    BACKUP_FILE,
    r"C:\Users\nguye\OneDrive\Documents\CODE\java\oop class 4\HotelBookingApp.java",
]

BASE_PKG = "vn.edu.ptit.oop.nhom08"
BASE_PKG_PATH = os.path.join(SRC_DIR, *BASE_PKG.split("."))

PACKAGE_MAP = {
    "HotelBookingApp": "",
    "BaseWindow": "common",
    "MainWindow": "ui",
    # Thành viên 2: Model Phòng
    "RoomStatus": "model",
    "Room": "model",
    "StandardRoom": "model",
    "DeluxeRoom": "model",
    "SuiteRoom": "model",
    # Thành viên 3: Model Khách, Booking & Exception
    "RoomNotAvailableException": "exception",
    "InvalidBookingDataException": "exception",
    "Gender": "model",
    "Guest": "model",
    "Booking": "model",
    # Thành viên 4: Chiến lược giá & Hóa đơn
    "PricingPolicy": "service",
    "StandardPricing": "service",
    "WeekendPricing": "service",
    "HolidayPricing": "service",
    "ServiceType": "model",
    "ServiceCharge": "model",
    "Invoice": "model",
    # Thành viên 5: Kho dữ liệu & Dịch vụ đặt phòng
    "HotelRepository": "repository",
    "InMemoryHotelRepository": "repository",
    "BookingService": "service",
    "DataFileManager": "util",
    # Thành viên 6: Sơ đồ phòng 2D & Chi tiết phòng
    "RoomMapPanel": "ui.panels",
    "RoomDetailPanel": "ui.panels",
    # Thành viên 7: Dialog Đặt phòng, Thêm Phòng/Tầng & Bảng
    "BookingTableModel": "ui",
    "BookingStatusCellRenderer": "ui",
    "BookingDialog": "ui.dialogs",
    "AddRoomFloorDialog": "ui.dialogs",
    # Thành viên 8: Hóa đơn đỏ, Lọc phòng & About
    "InvoiceDialog": "ui.dialogs",
    "InvoicePaperPanel": "ui.panels",
    "RoomFilterDialog": "ui.dialogs",
    "AboutDialog": "ui.dialogs",
    # Thành viên 9: Biểu đồ thống kê
    "ChartWindow": "ui",
    "BarChartPanel": "ui.panels",
    "LineChartPanel": "ui.panels",
}

def find_source_file():
    valid_files = [
        p for p in CANDIDATE_FILES
        if os.path.exists(p) and os.path.getsize(p) > 5000
    ]
    if not valid_files:
        return None
    # Chọn file vừa được bạn cập nhật mới nhất
    return max(valid_files, key=os.path.getmtime)

def split_java_file():
    source_path = find_source_file()
    if not source_path:
        print("Không tìm thấy file code gộp! Hãy kiểm tra lại file HotelBookingApp.java.")
        return

    print(f"Đang đọc code từ: {source_path}")
    with open(source_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Lưu bản sao dự phòng ra ngoài thư mục gốc
    with open(BACKUP_FILE, "w", encoding="utf-8") as bf:
        bf.write(content)

    # Xóa file HotelBookingApp.java nằm trực tiếp trong src/ (nếu có) để tránh trùng lặp class với gói nhom08
    raw_in_src = os.path.join(SRC_DIR, "HotelBookingApp.java")
    if os.path.exists(raw_in_src):
        os.remove(raw_in_src)

    imports = "\n".join(re.findall(r"^import\s+.*?;", content, flags=re.MULTILINE))
    pattern = re.compile(r"(?m)^(?:public\s+)?(?:abstract\s+)?(?:class|interface|enum)\s+(\w+)[^{]*\{")
    matches = list(pattern.finditer(content))

    all_subpackages = sorted(set(v for v in PACKAGE_MAP.values() if v))
    project_imports = f"import {BASE_PKG}.*;\n" + "\n".join(
        f"import {BASE_PKG}.{sub}.*;" for sub in all_subpackages
    )

    count = 0
    for match in matches:
        class_name = match.group(1)
        start_pos = match.start()

        brace_level = 0
        end_pos = len(content)
        for i in range(match.end() - 1, len(content)):
            if content[i] == "{":
                brace_level += 1
            elif content[i] == "}":
                brace_level -= 1
                if brace_level == 0:
                    end_pos = i + 1
                    break

        class_body = content[start_pos:end_pos].strip()
        if not class_body.startswith("public "):
            class_body = "public " + class_body

        subpkg = PACKAGE_MAP.get(class_name, "")
        full_pkg = f"{BASE_PKG}.{subpkg}" if subpkg else BASE_PKG
        target_dir = os.path.join(BASE_PKG_PATH, *subpkg.split(".")) if subpkg else BASE_PKG_PATH
        os.makedirs(target_dir, exist_ok=True)

        file_path = os.path.join(target_dir, f"{class_name}.java")
        file_content = (
            f"package {full_pkg};\n\n"
            f"{imports}\n"
            f"{project_imports}\n\n"
            f"{class_body}\n"
        )

        with open(file_path, "w", encoding="utf-8") as out:
            out.write(file_content)
        count += 1
        print(f"[{count:02d}] Đã tạo: src/{full_pkg.replace('.', '/')}/{class_name}.java")

    print("\n============================================================")
    print(f"HOÀN TẤT! Đã tách thành công {count} file vào src/vn/edu/ptit/oop/nhom08")
    print("Bây giờ bạn hãy mở file src/vn/edu/ptit/oop/nhom08/HotelBookingApp.java và bấm Run!")
    print("============================================================")

if __name__ == "__main__":
    split_java_file()