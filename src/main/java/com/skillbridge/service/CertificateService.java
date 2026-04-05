// src/main/java/com/skillbridge/service/CertificateService.java
package com.skillbridge.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.skillbridge.exception.ResourceNotFoundException;
import com.skillbridge.model.Certificate;
import com.skillbridge.model.Session;
import com.skillbridge.model.User;
import com.skillbridge.repository.CertificateRepository;
import com.skillbridge.repository.SessionRepository;
import com.skillbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Value("${certificate.output-dir:./certificates}")
    private String outputDir;

    /**
     * Generate a PDF certificate for a completed session.
     * Called automatically by SessionEventListener.
     */
    public Certificate generateCertificate(String sessionId) {

        // Idempotent – skip if already generated
        if (certificateRepository.findBySessionId(sessionId).isPresent()) {
            log.info("Certificate already exists for session {}", sessionId);
            return certificateRepository.findBySessionId(sessionId).get();
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        User learner = userRepository.findById(session.getLearnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found"));

        User mentor = userRepository.findById(session.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        // Create output directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            throw new RuntimeException("Cannot create certificate directory: " + e.getMessage());
        }

        String certNumber = "SKILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String filePath   = outputDir + "/" + certNumber + ".pdf";

        // Build the PDF
        buildPdf(filePath,
                learner.getName(),
                mentor.getName(),
                session.getTopic(),
                certNumber,
                session.getScheduledAt());

        // Save metadata to MongoDB
        Certificate cert = Certificate.builder()
                .sessionId(sessionId)
                .learnerId(session.getLearnerId())
                .mentorId(session.getMentorId())
                .learnerName(learner.getName())
                .mentorName(mentor.getName())
                .topic(session.getTopic())
                .certificateNumber(certNumber)
                .filePath(filePath)
                .issuedAt(LocalDateTime.now())
                .build();

        Certificate saved = certificateRepository.save(cert);
        log.info("Certificate {} generated for learner {}", certNumber, learner.getName());
        return saved;
    }

    // ── PDF Layout using iText 7 ──────────────────────────────────────────────

    private void buildPdf(String filePath,
                          String learnerName,
                          String mentorName,
                          String topic,
                          String certNumber,
                          LocalDateTime sessionDate) {
        try {
            PdfWriter   writer      = new PdfWriter(filePath);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document    document    = new Document(pdfDocument, PageSize.A4.rotate());
            document.setMargins(40, 60, 40, 60);

            // Fonts
            PdfFont titleFont  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont bodyFont   = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // Colors
            DeviceRgb blue      = new DeviceRgb(30, 64, 175);
            DeviceRgb lightBlue = new DeviceRgb(59, 130, 246);
            DeviceRgb gray      = new DeviceRgb(107, 114, 128);
            DeviceRgb darkGray  = new DeviceRgb(17, 24, 39);

            // ── Header ────────────────────────────────────────────────────────

            Paragraph header = new Paragraph("🎓 SkillBridge")
                    .setFont(titleFont)
                    .setFontSize(14)
                    .setFontColor(lightBlue)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph title = new Paragraph("Certificate of Completion")
                    .setFont(titleFont)
                    .setFontSize(32)
                    .setFontColor(blue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(title);

            // ── Blue separator line ───────────────────────────────────────────

            SolidLine line = new SolidLine(2f);
            line.setColor(lightBlue);
            LineSeparator separator = new LineSeparator(line);
            separator.setMarginTop(10);
            separator.setMarginBottom(20);
            document.add(separator);

            // ── Body ──────────────────────────────────────────────────────────

            document.add(new Paragraph("This certifies that")
                    .setFont(bodyFont)
                    .setFontSize(14)
                    .setFontColor(gray)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(learnerName)
                    .setFont(titleFont)
                    .setFontSize(28)
                    .setFontColor(darkGray)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5)
                    .setMarginBottom(5));

            document.add(new Paragraph("has successfully completed a mentorship session on")
                    .setFont(bodyFont)
                    .setFontSize(14)
                    .setFontColor(gray)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\"" + topic + "\"")
                    .setFont(titleFont)
                    .setFontSize(22)
                    .setFontColor(blue)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5)
                    .setMarginBottom(5));

            document.add(new Paragraph("under the expert guidance of " + mentorName)
                    .setFont(italicFont)
                    .setFontSize(14)
                    .setFontColor(gray)
                    .setTextAlignment(TextAlignment.CENTER));

            // ── Second separator ──────────────────────────────────────────────

            LineSeparator separator2 = new LineSeparator(new SolidLine(1f));
            separator2.setMarginTop(20);
            separator2.setMarginBottom(15);
            document.add(separator2);

            // ── Footer table ──────────────────────────────────────────────────

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            String dateStr = (sessionDate != null) ? sessionDate.format(fmt) : "N/A";

            Table footer = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
            footer.setWidth(UnitValue.createPercentValue(100));
            footer.setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Date cell
            Cell dateCell = new Cell()
                    .add(new Paragraph("Date Issued").setFont(titleFont)
                            .setFontSize(10).setFontColor(gray))
                    .add(new Paragraph(dateStr).setFont(bodyFont)
                            .setFontSize(11).setFontColor(darkGray))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            // Certificate number cell
            Cell certCell = new Cell()
                    .add(new Paragraph("Certificate No.").setFont(titleFont)
                            .setFontSize(10).setFontColor(gray))
                    .add(new Paragraph(certNumber).setFont(bodyFont)
                            .setFontSize(11).setFontColor(darkGray))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            // Website cell
            Cell siteCell = new Cell()
                    .add(new Paragraph("Issued By").setFont(titleFont)
                            .setFontSize(10).setFontColor(gray))
                    .add(new Paragraph("skillbridge.com").setFont(bodyFont)
                            .setFontSize(11).setFontColor(darkGray))
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);

            footer.addCell(dateCell);
            footer.addCell(certCell);
            footer.addCell(siteCell);
            document.add(footer);

            document.close();
            log.info("PDF written to: {}", filePath);

        } catch (IOException e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Read certificate PDF bytes for download endpoint.
     */
    public byte[] getCertificateBytes(String sessionId) throws IOException {
        Certificate cert = certificateRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found for session: " + sessionId));
        return Files.readAllBytes(Paths.get(cert.getFilePath()));
    }
}