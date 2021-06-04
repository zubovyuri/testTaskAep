import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("--Начало работы.");
        // Исходный файл
        File sourceFile = new File("src/main/resources/source_file.xml");
        // Файл с результатом
        File targetFile = new File("src/main/resources/target_file.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document source = builder.parse(sourceFile);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // XPath с подходящими 3D-объектами
            String findNeutralExpr = ".//Neutral[.//Origin]";
            NodeList neutralObjects = (NodeList) xpath.evaluate(findNeutralExpr, source, XPathConstants.NODESET);
            if (neutralObjects.getLength() == 0) {
                logger.error("В этом XML нет нужных объектов Neutral.");
            } else {
                logger.info("Нужные объекты найдены. Начало поиска и замены цвета.");
                // Новый элемент Actor.Color с красным цветом
                Element actorColor = source.createElement("Actor.Color");
                actorColor.setAttribute("R", "255");
                actorColor.setAttribute("G", "0");
                actorColor.setAttribute("B", "0");

                // Перебор найденных объектов Neutral и замена/добавление Actor.Color
                for (int i = 0; i < neutralObjects.getLength(); i++) {
                    Node neutralObject = neutralObjects.item(i);
                    //XPath с элементами Actor.Color
                    String findActorColorsExpr = ".//Actor.Color";
                    NodeList actorColors = (NodeList) xpath.evaluate(findActorColorsExpr, neutralObject, XPathConstants.NODESET);
                    if (actorColors.getLength() > 0) {
                        logger.info("Замена цвета.");
                        for (int j = 0; j < actorColors.getLength(); j++) {
                            neutralObject.replaceChild(actorColor, actorColors.item(j));
                        }
                    } else {
                        neutralObject.appendChild(actorColor);
                    }
                    removeElementsWhitespace(neutralObject, xpath);
                }
            }
            DOMSource domSource = new DOMSource(source);
            StreamResult result = new StreamResult(targetFile);
            transformer.transform(domSource, result);
        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода: " + e);
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            logger.error("Ошибка конфигурации трансформера: " + e);
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            logger.error("Ошибка парсера XML: " + e);
            e.printStackTrace();
        } catch (SAXException e) {
            logger.error("Ошибка SAX: " + e);
            e.printStackTrace();
        } catch (TransformerException e) {
            logger.error("Ошибка трансформера: " + e);
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            logger.error("Ошибка XPath выражения: " + e);
            e.printStackTrace();
        }
        logger.info("--Конец работы.");
    }

    public static void removeElementsWhitespace(Node node, XPath xpath) throws XPathExpressionException {
        NodeList nl = (NodeList) xpath.evaluate("//text()[normalize-space(.)='']", node, XPathConstants.NODESET);
        for (int j = 0; j < nl.getLength(); ++j) {
            Node currentNode = nl.item(j);
            currentNode.getParentNode().removeChild(currentNode);
        }
    }
}
