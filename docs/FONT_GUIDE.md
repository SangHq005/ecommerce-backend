# HƯỚNG DẪN FONT CHỮ VÀ ĐỊNH DẠNG CHO ĐỒ ÁN

## 1. FONT CHỮ CHUẨN CHO ĐỒ ÁN

### 1.1. Font chữ văn bản chính
- **Font**: **Times New Roman**
- **Kích thước**: **12pt** hoặc **13pt**
- **Áp dụng cho**: Nội dung chính, đoạn văn, mô tả

### 1.2. Font chữ tiêu đề
- **Font**: **Arial** hoặc **Calibri**
- **Kích thước**: 
  - Tiêu đề chương: **16pt** (Bold)
  - Tiêu đề mục: **14pt** (Bold)
  - Tiêu đề phụ: **13pt** (Bold)
- **Áp dụng cho**: Tiêu đề các chương, mục, bảng biểu

### 1.3. Font chữ bảng biểu
- **Font**: **Times New Roman** hoặc **Arial**
- **Kích thước**: **11pt** hoặc **12pt**
- **Áp dụng cho**: Bảng dữ liệu, danh sách

### 1.4. Font chữ code
- **Font**: **Courier New** hoặc **Consolas**
- **Kích thước**: **10pt** hoặc **11pt**
- **Áp dụng cho**: Mã nguồn, câu lệnh SQL, JSON

---

## 2. QUY TẮC ĐỊNH DẠNG

### 2.1. Khoảng cách dòng (Line Spacing)
- **Văn bản chính**: 1.5 lines hoặc 1.2 lines
- **Bảng biểu**: Single (1.0) hoặc 1.15
- **Code**: Single (1.0)

### 2.2. Căn lề (Margin)
- **Trên**: 2.5cm
- **Dưới**: 2.5cm
- **Trái**: 3.5cm
- **Phải**: 2cm

### 2.3. Đánh số trang
- **Vị trí**: Góc dưới bên phải
- **Font**: Times New Roman, 12pt
- **Bắt đầu đánh số từ**: Trang đầu tiên sau mục lục

---

## 3. ĐỊNH DẠNG BẢNG BIỂU

### 3.1. Tiêu đề bảng
- **Font**: **Arial**, **14pt**, **Bold**
- **Căn giữa**
- **Đánh số**: Bảng 1.1, Bảng 1.2, ...

### 3.2. Nội dung bảng
- **Font**: **Times New Roman**, **11pt** hoặc **12pt**
- **Header**: **Bold**, nền xám nhạt
- **Căn lề**: 
  - Cột số: Căn phải
  - Cột text: Căn trái
  - Cột ngày tháng: Căn giữa

### 3.3. Ví dụ định dạng bảng

```
Bảng 1.1: Danh sách các bảng cơ sở dữ liệu

Font: Times New Roman, 11pt
Header: Bold, nền #F2F2F2
Border: 1pt, màu đen
```

---

## 4. ĐỊNH DẠNG HÌNH ẢNH

### 4.1. Tiêu đề hình
- **Font**: **Arial**, **12pt**, **Bold**
- **Căn giữa**
- **Đánh số**: Hình 1.1, Hình 1.2, ...

### 4.2. Chú thích
- **Font**: **Times New Roman**, **11pt**
- **Căn giữa**, **Italic**

---

## 5. ĐỊNH DẠNG CODE

### 5.1. Code block
- **Font**: **Courier New**, **10pt**
- **Nền**: #F5F5F5
- **Border**: 1pt, màu #CCCCCC
- **Padding**: 5pt

### 5.2. Inline code
- **Font**: **Courier New**, **11pt**
- **Nền**: #F0F0F0
- **Padding**: 2pt

---

## 6. MẪU ĐỊNH DẠNG CHO TÀI LIỆU DATABASE_SCHEMA.md

Khi xuất sang Word/PDF cho đồ án, nên áp dụng:

### 6.1. Tiêu đề chính
```
Font: Arial, 16pt, Bold
Căn giữa
Khoảng cách: 12pt sau
```

### 6.2. Tiêu đề bảng
```
Font: Arial, 14pt, Bold
Căn trái
Khoảng cách: 6pt trước, 6pt sau
```

### 6.3. Nội dung bảng
```
Font: Times New Roman, 11pt
Header: Bold, nền #E8E8E8
Border: 0.5pt, màu #000000
Căn lề: 
  - STT: Căn giữa
  - Thuộc tính: Căn trái
  - Kiểu dữ liệu: Căn trái
  - Ràng buộc: Căn trái
  - Diễn giải: Căn trái
```

---

## 7. CÔNG CỤ CHUYỂN ĐỔI

### 7.1. Markdown → Word
- Sử dụng **Pandoc**: `pandoc DATABASE_SCHEMA.md -o DATABASE_SCHEMA.docx`
- Hoặc sử dụng online converter: Dillinger, StackEdit

### 7.2. Định dạng trong Word
1. Mở file Word
2. Chọn toàn bộ văn bản (Ctrl+A)
3. Đặt font: Times New Roman, 12pt
4. Đặt line spacing: 1.5
5. Format từng phần theo hướng dẫn trên

---

## 8. LƯU Ý

1. **Nhất quán**: Sử dụng cùng một font và kích thước cho cùng loại nội dung
2. **Dễ đọc**: Tránh font quá nhỏ (<10pt) hoặc quá lớn (>16pt)
3. **Chuyên nghiệp**: Ưu tiên font chuẩn (Times New Roman, Arial) thay vì font nghệ thuật
4. **In đen trắng**: Đảm bảo bảng biểu vẫn rõ ràng khi in đen trắng
5. **Kiểm tra**: In thử một trang để kiểm tra định dạng trước khi in toàn bộ

---

## 9. FONT CHỮ ĐỀ XUẤT CHO ĐỒ ÁN VIỆT NAM

### 9.1. Phương án 1 (Chuẩn)
- **Văn bản**: Times New Roman, 12pt
- **Tiêu đề**: Arial, 14-16pt, Bold
- **Code**: Courier New, 10pt

### 9.2. Phương án 2 (Hiện đại)
- **Văn bản**: Calibri, 11pt
- **Tiêu đề**: Calibri, 14-16pt, Bold
- **Code**: Consolas, 10pt

### 9.3. Phương án 3 (Học thuật)
- **Văn bản**: Times New Roman, 12pt
- **Tiêu đề**: Times New Roman, 14-16pt, Bold
- **Code**: Courier New, 10pt

---

## 10. TEMPLATE CSS CHO MARKDOWN (Nếu xuất HTML)

```css
body {
    font-family: 'Times New Roman', serif;
    font-size: 12pt;
    line-height: 1.5;
    margin: 2.5cm 3.5cm 2.5cm 2cm;
}

h1 {
    font-family: 'Arial', sans-serif;
    font-size: 16pt;
    font-weight: bold;
    text-align: center;
}

h2 {
    font-family: 'Arial', sans-serif;
    font-size: 14pt;
    font-weight: bold;
}

table {
    font-family: 'Times New Roman', serif;
    font-size: 11pt;
    border-collapse: collapse;
    width: 100%;
}

table th {
    background-color: #E8E8E8;
    font-weight: bold;
    padding: 8px;
    border: 0.5pt solid #000000;
}

table td {
    padding: 6px;
    border: 0.5pt solid #000000;
}

code {
    font-family: 'Courier New', monospace;
    font-size: 10pt;
    background-color: #F5F5F5;
    padding: 2pt;
}
```

---

**Lưu ý**: Tùy theo yêu cầu cụ thể của trường/khoa, có thể điều chỉnh font chữ và kích thước cho phù hợp.
