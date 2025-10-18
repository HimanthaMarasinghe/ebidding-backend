package com.e.bidding.user_service.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
@Service
public class PDFGenerator {
    @Autowired
    QRGenerator qrGenerator;

    public File generateOfficialLetter(String recipientName,
                                              Integer itemId,
                                              String itemName,
                                              String itemDescription , String claimDate,Integer amount,String location,Integer winningPlace) throws Exception {

        // Generate QR Code (based on itemId)
        String qrContent = itemId.toString();
        BufferedImage qrImage = qrGenerator.generateQRCodeImage(qrContent, 150, 150);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);

        File pdfFile = File.createTempFile("winning_letter_", ".pdf");
        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);
        doc.setMargins(40, 50, 40, 50);

        // ---- Header ----
        Paragraph heading = new Paragraph("SRI LANKAN CUSTOMS – E-BIDDING SYSTEM")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(heading);

        Paragraph subHeading = new Paragraph("Sri Lanka Customs,\nNo. 40, Main Street, Colombo 11, Sri Lanka.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(subHeading);

        doc.add(new LineSeparator(new SolidLine()).setWidth(UnitValue.createPercentValue(100)));
        // ---- Recipient ----
        doc.add(new Paragraph("\nTo:\n" + recipientName + "\n")
                .setFontSize(11)
                .setMarginBottom(15));

        // ---- Body ----
        doc.add(new Paragraph("Official Claim Letter for the Item You Won Through Bidding ").setBold().setFontSize(12).setUnderline().setMarginBottom(10));
        String bodyText = getString(recipientName, winningPlace);

        doc.add(new Paragraph(bodyText).setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED));

        // ---- Item Details Table ----
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 7}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.addCell(new Cell().add(new Paragraph("Item ID:").setBold()));
        table.addCell(new Cell().add(new Paragraph(itemId.toString())));
        table.addCell(new Cell().add(new Paragraph("Item Name:").setBold()));
        table.addCell(new Cell().add(new Paragraph(itemName)));
        table.addCell(new Cell().add(new Paragraph("Description:").setBold()));
        table.addCell(new Cell().add(new Paragraph(itemDescription)));
        table.addCell(new Cell().add(new Paragraph("Location:").setBold()));
        table.addCell(new Cell().add(new Paragraph(location)));
        table.addCell(new Cell().add(new Paragraph("Amount To Pay:").setBold()));
        table.addCell(new Cell().add(new Paragraph("LKR "+amount.toString()+".00")));

        doc.add(table);

        doc.add(new Paragraph("\nIMPORTANT NOTICE: You are required to claim the awarded item within three (03) days from the date of this notice (on or before: " + claimDate + "). Failure to do so will result in the item being automatically offered to the next eligible bidder. Please note that the deposit you have made to the system will be forfeited if the item is not claimed within the stipulated period.")
                .setFontSize(10)
                .setBold());

        doc.add(new Paragraph("\nPlease note that this document serves as an official confirmation from Sri Lanka Customs.")
                .setFontSize(10));



        // ---- QR Code ----
        doc.add(new Paragraph("Please use the following QR code to assist the officers in the item claiming process:"));
        Image qr = new Image(ImageDataFactory.create(baos.toByteArray()));
        qr.setWidth(100);
        qr.setHeight(100);
        doc.add(qr.setMarginBottom(10));

        // ---- Footer ----
        doc.add(new Paragraph(
                "This is an electronically generated document by the Sri Lankan Customs E-Bidding  System. " +
                        "No signature is required.\nThank you for participating in the Sri Lanka Customs electronic bidding process."
        ).setFontSize(9).setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return pdfFile;
    }

    private static String getString(String recipientName, Integer winningPlace) {
        String bodyText;
        if(winningPlace.equals(1)){

             bodyText = "Dear " + recipientName + ",\n" +
                    "Congratulations! This letter serves as an official notification that you have successfully won the item listed below through the Sri Lanka Customs E‑Bidding System. " +
                    "You are hereby entitled to claim ownership of the awarded item as detailed below.\n" +
                    "You are kindly requested to visit the Sri Lanka Customs premises within three (03) days from the date of this notice to claim your item. " +
                    "Please ensure that you bring this official letter along with a valid form of identification for verification and collection purposes.\n\n" +
                    "Please be prepared to settle the bid amount on the day of collection.\n" +
                    "The details of the awarded item are as follows:";
        }
        else {
             bodyText = "Dear " + recipientName + ",\n" +
                    "Congratulations! This letter serves as an official notification that you have successfully won the item listed below through the Sri Lanka Customs E‑Bidding System. " +
                     "Since the previous bidder did not claim the item within the given period, you have been selected as the next eligible bidder, " +
                     "You are hereby entitled to claim ownership of the awarded item as detailed below.\n" +
                    "You are kindly requested to visit the Sri Lanka Customs premises within three (03) days from the date of this notice to claim your item. " +
                    "Please ensure that you bring this official letter along with a valid form of identification for verification and collection purposes.\n\n" +
                    "Please be prepared to settle the bid amount on the day of collection.\n" +
                    "The details of the awarded item are as follows:";
        }
        return bodyText;
    }
}