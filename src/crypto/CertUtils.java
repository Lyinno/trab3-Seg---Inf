package crypto;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

public class CertUtils {

    public static X509Certificate carregarCertificado(String caminhoArquivo) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(caminhoArquivo));

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }

    public static PublicKey obterChavePublica(X509Certificate certificado) {
        return certificado.getPublicKey();
    }

    public static String obterNome(X509Certificate certificado) {
        String subject = certificado.getSubjectX500Principal().getName("RFC1779");

        String[] partes = subject.split(",");
        for (String parte : partes) {
            parte = parte.trim();
            if (parte.startsWith("CN=")) {
                return parte.substring(3).trim();
            }
        }

        return subject;
    }

    public static String obterEmail(X509Certificate certificado) {
        try {
            Collection<List<?>> nomes = certificado.getSubjectAlternativeNames();

            if (nomes != null) {
                for (List<?> item : nomes) {
                    Integer tipo = (Integer) item.get(0);
                    Object valor = item.get(1);

                    // tipo 1 = rfc822Name, ou seja, e-mail
                    if (tipo == 1 && valor != null) {
                        return valor.toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            // tenta pelo subject abaixo
        }

        String subject = certificado.getSubjectX500Principal().getName("RFC1779");
        String[] partes = subject.split(",");

        for (String parte : partes) {
            parte = parte.trim();

            if (parte.startsWith("EMAILADDRESS=")) {
                return parte.substring("EMAILADDRESS=".length()).trim();
            }

            if (parte.startsWith("emailAddress=")) {
                return parte.substring("emailAddress=".length()).trim();
            }

            if (parte.startsWith("E=")) {
                return parte.substring("E=".length()).trim();
            }

            if (parte.startsWith("OID.1.2.840.113549.1.9.1=")) {
                return parte.substring("OID.1.2.840.113549.1.9.1=".length()).trim();
            }
        }

        return null;
    }

    public static String resumoCertificado(X509Certificate certificado) {
        StringBuilder sb = new StringBuilder();

        sb.append("Versão: ").append(certificado.getVersion()).append("\n");
        sb.append("Série: ").append(certificado.getSerialNumber()).append("\n");
        sb.append("Validade inicial: ").append(certificado.getNotBefore()).append("\n");
        sb.append("Validade final: ").append(certificado.getNotAfter()).append("\n");
        sb.append("Tipo de assinatura: ").append(certificado.getSigAlgName()).append("\n");
        sb.append("Emissor: ").append(certificado.getIssuerX500Principal().getName()).append("\n");
        sb.append("Sujeito: ").append(certificado.getSubjectX500Principal().getName()).append("\n");
        sb.append("E-mail: ").append(obterEmail(certificado)).append("\n");

        return sb.toString();
    }

    public static String lerCertificadoComoTexto(String caminhoArquivo) throws Exception {
        return new String(Files.readAllBytes(Paths.get(caminhoArquivo)), "UTF-8");
    }

    public static X509Certificate carregarCertificadoDeTexto(String pem) throws Exception {
        byte[] bytes = pem.getBytes("UTF-8");

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
    }
}