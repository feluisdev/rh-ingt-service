package cv.igrp.RH_Service.sigdi.application.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExportDocumentHelper {

  public static byte[] createCsv(List<String[]> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("\uFEFF"); // UTF-8 BOM for Microsoft Excel
    for (String[] row : rows) {
      for (int i = 0; i < row.length; i++) {
        String val = row[i] != null ? row[i] : "";
        if (val.contains(";") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
          sb.append("\"").append(val.replace("\"", "\"\"")).append("\"");
        } else {
          sb.append(val);
        }
        if (i < row.length - 1) sb.append(";");
      }
      sb.append("\r\n");
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public static byte[] createSimplePdf(String title, List<String> lines) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      List<Integer> offsets = new ArrayList<>();
      offsets.add(0);

      // 1 0 obj Catalog
      offsets.add(out.size());
      out.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

      // 2 0 obj Pages
      offsets.add(out.size());
      out.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

      // 3 0 obj Page
      offsets.add(out.size());
      out.write("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

      // 5 0 obj Font
      offsets.add(out.size());
      out.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

      // 4 0 obj Stream
      StringBuilder stream = new StringBuilder();
      stream.append("BT\n/F1 14 Tf\n50 740 Td\n(").append(cleanPdfText(title)).append(") Tj\n/F1 9 Tf\n");
      int y = 710;
      for (String line : lines) {
        if (y < 40) break;
        stream.append("1 0 0 1 50 ").append(y).append(" Tm\n");
        stream.append("(").append(cleanPdfText(line)).append(") Tj\n");
        y -= 13;
      }
      stream.append("ET\n");

      byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);
      offsets.add(out.size());
      out.write(("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
      out.write(streamBytes);
      out.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));

      ByteArrayOutputStream finalDoc = new ByteArrayOutputStream();
      finalDoc.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
      int shift = 9;
      byte[] body = out.toByteArray();
      finalDoc.write(body);

      int startXref = finalDoc.size();
      finalDoc.write("xref\n0 6\n".getBytes(StandardCharsets.US_ASCII));
      finalDoc.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));

      int[] objOffsets = new int[6];
      objOffsets[1] = offsets.get(1) + shift;
      objOffsets[2] = offsets.get(2) + shift;
      objOffsets[3] = offsets.get(3) + shift;
      objOffsets[5] = offsets.get(4) + shift;
      objOffsets[4] = offsets.get(5) + shift;

      for (int i = 1; i <= 5; i++) {
        finalDoc.write(String.format("%010d 00000 n \n", objOffsets[i]).getBytes(StandardCharsets.US_ASCII));
      }

      finalDoc.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + startXref + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
      return finalDoc.toByteArray();
    } catch (IOException e) {
      return title.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static String cleanPdfText(String text) {
    if (text == null) return "";
    return text.replace("\\", "\\\\")
               .replace("(", "\\(")
               .replace(")", "\\)")
               .replaceAll("[^\\x20-\\x7E]", " ");
  }
}
