package bbm.label;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.modelmapper.internal.util.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Component
public class Labeler {
    /**
     * QRCode width.
     */
    public static final int CODE_WIDTH = 160;
    /**
     * QRCode height.
     */
    public static final int CODE_HEIGHT = 160;
    /**
     * Label template file name. Must be within classpath.
     */
    public static final String LABEL_TEMPLATE_PDF = "Label58x40.pdf";
    public static final int NAME_LINE_LEN = 23;
    public static final int DESC_LINE_LEN = 11;

    /**
     * Gets QRCode image.
     *
     * @param code code
     * @return QRCode image
     * @throws WriterException
     */
    public BufferedImage getQRCode(String code) throws WriterException {
        BitMatrix bm = new QRCodeWriter().encode(code,
                BarcodeFormat.QR_CODE, CODE_WIDTH, CODE_HEIGHT);

        BufferedImage image = new BufferedImage(bm.getWidth(),
                bm.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, CODE_WIDTH, CODE_HEIGHT);
        graphics.setColor(Color.BLACK);
        // Write message under the QR-code
//        graphics.drawString(content, 30, image.getHeight() - graphics.getFont().getSize());

        //Write Bit Matrix as image
        for (int i = 0; i < CODE_HEIGHT; i++) {
            for (int j = 0; j < CODE_WIDTH; j++) {
                if (bm.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        return image;
    }

    private String format(String text, int size) {
        Assert.notNull(text);
        return WordUtils.wrap(text, size, null, true);
    }

    /**
     * Generate PDF Label byte array stream.
     * Takes classpath resource @link LABEL_TEMPLATE_PDF as template.
     * Template must have :
     * PDAcroForm with fields name, description
     * image
     *
     * @param image embedded image
     * @param name  name
     * @param desc  description(nullable)
     * @return pdf doc byte array
     * @throws IOException PDF, image, stream exception could occur
     */
    public ByteArrayOutputStream generateLabel(BufferedImage image, String name, String desc)
            throws IOException {
        PDDocument labelTemplate = PDDocument
                .load(new ClassPathResource(LABEL_TEMPLATE_PDF).getInputStream());
        PDDocumentCatalog dc = labelTemplate.getDocumentCatalog();
        PDAcroForm af = dc.getAcroForm();

        af.getField("name").setValue(format(name, NAME_LINE_LEN));
        af.getField("description")
                .setValue(format(Optional.ofNullable(desc).orElse(""), DESC_LINE_LEN));
        af.flatten(af.getFields(), true);

        COSObject imageHolder = labelTemplate.getDocument()
                .getObjects()
                .stream()
                .filter(c -> c.getDictionaryObject(COSName.SUBTYPE) == COSName.IMAGE)
                .findFirst()
                .orElseThrow(
                        () -> new IOException("Code image placeholder not found in template.")
                );

        PDImageXObject replacement = LosslessFactory.createFromImage(labelTemplate, image);
        imageHolder.setObject(replacement.getCOSObject());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        labelTemplate.save(os);
        labelTemplate.close();
        return os;
    }
}
