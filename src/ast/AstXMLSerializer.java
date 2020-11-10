package ast;

import javax.xml.bind.*;
import java.io.*;

public class AstXMLSerializer {
    public void serialize(AstNode ast, String outfilename) throws IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Program.class);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            var outWriter = new FileOutputStream(new File(outfilename));
            try {
                marshaller.marshal(ast, outWriter);
            } finally {
                outWriter.close();
            }
        } catch (PropertyException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public Program deserialize(File file) {
        try {
            JAXBContext jc = JAXBContext.newInstance(Program.class);

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Program) unmarshaller.unmarshal(file);
        } catch (PropertyException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
