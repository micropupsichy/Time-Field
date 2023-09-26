/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.micropupsichi.timefield;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;

/**
 *
 * @author test
 */
public class DateEntryFieldManager {
        private final SimpleDateFormat SDF_TS = new SimpleDateFormat ("dd.MM.yyyy HH:mm:ss.SSS"); //формат ввода TimeStamp
        private final SimpleDateFormat SDF_T = new SimpleDateFormat ("HH:mm:ss.SSS");             //формат ввода Date
        private Comparable STARTTIMESTAMP;                                                              //начальная позиция по дате и времени
        private final String DATE_TIME_MASK = "##.##.#### ##:##:##.###";                                //маска для TimeStamp
        private final String TIME_MASK = "##:##:##.###";                                                //маска для Date
        private JFormattedTextField jftf;                                                               //ссылка на текстовое поле
        private SimpleDateFormat currentSDF = new SimpleDateFormat("dd.MM.yyyy");
        private String currentMASK;
        private boolean onlyTime = false;
        
        public DateEntryFieldManager(Comparable startTimeStamp, String types) {
            String type[] = types.trim().split(",");
            for (String elem : type){
                if (elem.equals("Integer")){
                    STARTTIMESTAMP = startTimeStamp;
                }else if (elem.equals("Double")){
                    STARTTIMESTAMP = Double.valueOf(startTimeStamp.toString());
                    break;
                }else{
                    STARTTIMESTAMP = startTimeStamp;
                    break;
                }
            }
        }
        //подключение слушателей фокуса
        private void addJFormattedTextFieldListener(){
            jftf.addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent e) {
                    //когда в фокусе - шрифт черный
                    jftf.setForeground(Color.black);
                    jftf.getCaret().setVisible(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    //если дата не валидна
                    if (!isDateValid()){
                        //красим в красный
                        jftf.setForeground(Color.red);
                    }else{
                        //иначе красим в зеленый
                        jftf.setForeground(Color.green);
                    }
                    jftf.getCaret().setVisible(false);
                }
            });
        }
        //метод для проверки даты на год по умолчанию
        private void checkBaseYear() throws ParseException{
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Date fristDate = currentSDF.parse("01.01.1970");
            cal1.setTime((Date) STARTTIMESTAMP);
            cal2.setTime(fristDate);
            onlyTime = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            currentMASK = !onlyTime ? DATE_TIME_MASK : TIME_MASK;
            currentSDF = !onlyTime ? SDF_TS : SDF_T;
        }
        //метод для привязки к полю
        public void attachedTo(JFormattedTextField jftf){
            if (jftf!=null){
                this.jftf = jftf;
                createModel();
                    if (STARTTIMESTAMP instanceof Date){
                        try {
                            //проверяем на 01.01.1970
                            checkBaseYear();
                            MaskFormatter dateMask = new MaskFormatter(currentMASK);
                            //символ по умолчанию - нуль
                            dateMask.setPlaceholderCharacter((char) 48);
                            //устанавливаем маску в поле
                            dateMask.install(jftf);
                        } catch (ParseException ex) {
                            
                        }
                    }else if (STARTTIMESTAMP instanceof Integer){
                        jftf.setText("0");
                    }else if (STARTTIMESTAMP instanceof Double){
                        jftf.setText("0.0");
                    }else{
                        throw new UnsupportedOperationException();
                    }
                    addJFormattedTextFieldListener();
            }else{
                throw new NullPointerException();
            }
        }
        
        //метод для получения временной отметки из поля
        public Comparable getTime(){
	//если поле было подключено и не было сброшено
            if (jftf!=null && jftf.getFocusListeners().length!=0){
                //сохраняем здесь объект date для выдачи и форматированной печати в поле
                Date outputDate;
                if (isDateValid()){
                    //если дата валидна, возвращаем полностью
                    try {
                        outputDate = currentSDF.parse(jftf.getText());
                        jftf.setText(currentSDF.format(outputDate));
                        return ((Comparable) outputDate);
                    } catch (ParseException e) {
                        try{
                            jftf.setText(""+Integer.valueOf(jftf.getText()));
                            return ((Comparable) Integer.parseInt(jftf.getText()));
                        } catch (NumberFormatException ex){
                            try{
                                jftf.setText(""+Double.valueOf(jftf.getText()));
                                return ((Comparable) Double.parseDouble(jftf.getText()));
                            } catch (NumberFormatException exc){

                            }
                        }
                    }
                }else{
                    if (STARTTIMESTAMP instanceof Date){
                        outputDate = setCompletedDate(false);
                        jftf.setText(currentSDF.format(outputDate));
                        if (isDateValid()){
                            return ((Comparable) outputDate);
                        }else{
                            outputDate = setCompletedDate(true);
                            jftf.setText(currentSDF.format(outputDate));
                            if (isDateValid()){
                                return (Comparable) outputDate;
                            }else{
                                jftf.setText(currentSDF.format(STARTTIMESTAMP));
                                return STARTTIMESTAMP;
                            }
                        }
                    }else{
                        jftf.setText(STARTTIMESTAMP.toString());
                        return STARTTIMESTAMP;
                    }
                }
            }else{
                throw new NullPointerException();
            }
            //если не валидна
            return null;        
        }
        
        //метод для возвращения состояния поля по умолчанию
        public void detach(){
            detachField(jftf);
        }
        //публичный статически метод для сброса состояния поля
        public static void detachField(JFormattedTextField jftf){
            //получаем список подключенных слушателей фокуса
            FocusListener[] focusListeners = jftf.getFocusListeners();
            //и удаляем их
            for (FocusListener focusListener : focusListeners){
                jftf.removeFocusListener(focusListener);
            }
            //аналогично со слушателями клавиатуры
            KeyListener[] keyListeners = jftf.getKeyListeners();
            for (KeyListener keyListener : keyListeners){
                jftf.removeKeyListener(keyListener);
            }
            //устанавливаем документ и цвет по умолчанию
            jftf.setDocument(new PlainDocument());
            jftf.setForeground(Color.black);
            jftf.getCaret().setVisible(false);
        }
        //метод для обработки ввода временной отметки типа date
        private Date setCompletedDate(boolean overwriteNull){
            try {
                //заменяем нули на символы из стартовой позиции, кроме времени
                //если renull=true заменяем все нули, даже во времени
                String split = "[ ;:.,-]";
                String[] partsField = jftf.getText().split(split);
                String[] partsDate = currentSDF.format(STARTTIMESTAMP).split(split);
                int limit = Math.min(partsField.length, partsDate.length);
                String[] splitters = new String[limit-1];
                int ind=0;
                for (String splitter : jftf.getText().split("[0-9]")){
                    if (splitter.length()!=0){
                        splitters[ind++]=splitter;
                    }
                }
                ind=0;
                StringBuilder now = new StringBuilder();
                for (int i=0; i<limit;i++){
                    if (Integer.parseInt(partsField[i])==0 && ((partsField.length>2 && i<3) || overwriteNull)){
                        now.append(partsDate[i]);
                    }else{
                        now.append(partsField[i]);
                    }
                    //если следующее поле даты заполнено, прекращаем перезаписывать нулевые поля
                    if (i>2 && i<limit-1 && Integer.parseInt(partsField[i+1])!=0){
                        overwriteNull=false;
                    }
                    if (i<limit-1){
                        now.append(splitters[ind++]);
                    }
                }
                return currentSDF.parse(now.toString());
            } catch (ParseException ex) {
                
            }
            return null;
        }
        
        //проверка на валидность
        private boolean isDateValid(){ 
            try {
                return ((Comparable) currentSDF.parse(jftf.getText())).compareTo(STARTTIMESTAMP)>=0;
            } catch (ParseException | ClassCastException e) {
                try{
                    return ((Comparable) Integer.parseInt(jftf.getText())).compareTo(STARTTIMESTAMP)>=0;
                }catch (NumberFormatException | ClassCastException ex){
                    try{
                        return ((Comparable) Double.parseDouble(jftf.getText())).compareTo(STARTTIMESTAMP)>=0;
                    }catch (NumberFormatException | ClassCastException exc){

                    }
                }
            }
            return false;
        }
        
        private void createModel() {
            //создаем экземпляр документа
            DocumentFields doc = new DocumentFields();
            //подключаем слушатели
            doc.addDocumentListener();
            //устанавливаем на поле
            jftf.setDocument(doc);
        }
        //класс документа
        public class DocumentFields extends PlainDocument{
            private int lastDotIndex=0;                 //Последний индекс точки в поле
            private int currentCaretPosition=0;         //Текущая позиция каретки (курсора ввода)
            private boolean[] isDefault = {true, true}; //Флаг начального состояния поля
            
            @Override 
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                char[] source = str.toCharArray();
                char[] result = new char[source.length];
                int j = 0;
                for (int i = 0; i < result.length; i++) {
                    //it is ok for the first char to be '-'
                    if (STARTTIMESTAMP instanceof Date && (Character.isDigit(source[i])||source[i] == (char)32||source[i] == (char)46||source[i] == (char)58) 
                            || STARTTIMESTAMP instanceof Integer && Character.isDigit(source[i]) 
                            || STARTTIMESTAMP instanceof Double && (Character.isDigit(source[i]) || source[i] == (char)46)){
                        result[j++] = source[i];
                    }else if (offs==0 && i==0 && source[i]=='-'){
                        result[j++] = source[i];
                    }else{
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                super.insertString(offs, new String(result, 0, j), a);
            }
            
            private void  setCorrectInteger(boolean isRemove){ //метод контрля ввода для Integer
                Runnable doAssist = new Runnable() {
                    @Override
                    public void run() {
                        currentCaretPosition = jftf.getCaretPosition();
                        String inputDate = jftf.getText();
                        if (inputDate.length()==0){ 
                            jftf.setText("0");
                            jftf.setCaretPosition(0);
                            isDefault[0]=true;
                            //если длина строки равна 2 и она оканчиватеся на 0 и поле находится в начальном состоянии и метод вызван не при удалении символа
                        } else if (inputDate.length()==2 && inputDate.endsWith("0") && isDefault[0] && !isRemove){ 
                            jftf.setText(inputDate.substring(0, inputDate.length()-1)); // удаляем начальный 0
                            isDefault[0]=false;
                        } else if (inputDate.length()==2 && inputDate.startsWith("0") && isDefault[0] && !isRemove){
                            jftf.setText(inputDate.substring(1, inputDate.length()));
                            isDefault[0]=false;
                        }   
                    }
                };
                SwingUtilities.invokeLater(doAssist);
            }
            
            private void setCorrectDouble(boolean isRemove){ //метод для контроля ввода для Double
                Runnable doAssist = new Runnable() {
                    @Override
                    public void run() {
                        currentCaretPosition = jftf.getCaretPosition();
                        String inputDate = jftf.getText();
                        String[] partsDouble;
                        int dotIndex;
                         if ((inputDate.indexOf(".")) == -1){  //если точка не найдена
                             if (inputDate.length()>lastDotIndex){ //если последний индекс точки меньше чем длина строки
                                jftf.setText(inputDate.substring(0, lastDotIndex)+"."+inputDate.substring( lastDotIndex)); //добавляем точку
                                jftf.setCaretPosition(currentCaretPosition);
                             }else { //устанавливаем значение по умолчанию
                                jftf.setText("0.0");
                                isDefault=new boolean[]{true, true};
                             } 
                        } else if ((dotIndex = inputDate.indexOf(".")) != inputDate.lastIndexOf(".")) { //если в строке несколько точек
                            inputDate = inputDate.replaceAll("\\.", ""); 
                            inputDate = inputDate.substring(0, dotIndex)+"."+inputDate.substring(dotIndex); 
                            jftf.setText(inputDate); //оставляем первую встретившуюся
                        } else {
                            lastDotIndex = (inputDate.indexOf("."));
                            partsDouble = inputDate.split("\\.");
                            if (partsDouble[0].length()==0){ //если целая часть остутствует
                                jftf.setText("0."+partsDouble[1]);
                                jftf.setCaretPosition(currentCaretPosition);
                                isDefault[0]=true;
                                //если длина целой части строки равна 2 и она оканчиватеся на 0 и поле находится в начальном состоянии и метод вызван не при удалении символа
                            } else if (partsDouble[0].length()==2 && partsDouble[0].startsWith("0") && isDefault[0] && !isRemove){
                                jftf.setText(partsDouble[0].substring(1, partsDouble[0].length())+"."+partsDouble[1]);
                                jftf.setCaretPosition(currentCaretPosition-1);
                                isDefault[0]=false;
                            }
                            if (partsDouble.length < 2){ //если дробная часть остутствует
                                jftf.setText(partsDouble[0]+".0");
                                jftf.setCaretPosition(currentCaretPosition);
                                isDefault[1]=true;
                                //если длина дробной части строки равна 2 и она оканчиватеся на 0 и поле находится в начальном состоянии и метод вызван не при удалении символа
                            } else if (partsDouble[1].length()==2 && partsDouble[1].endsWith("0") && isDefault[1] && !isRemove){
                                jftf.setText(partsDouble[0]+"."+(partsDouble[1].substring(0, partsDouble[1].length()-1))); //удаляем начальный 0 дробной части
                                jftf.setCaretPosition(currentCaretPosition);
                                isDefault[1]=false;
                            }
                        } 
                    }
                };
                SwingUtilities.invokeLater(doAssist);
            }
            private void addKeyListener(){
                // обрабатываем вставку в поле неформатированной строки
                jftf.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_V) {
                            if (STARTTIMESTAMP instanceof Double){
                                setCorrectDouble(false);
                            } else if (STARTTIMESTAMP instanceof Integer)
                                setCorrectInteger(false);
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                    }
                });
            }
            protected void addDocumentListener(){
                addKeyListener();
                addDocumentListener(new DocumentListener(){
                    
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        if (STARTTIMESTAMP instanceof Double){
                            setCorrectDouble(true);
                        }else if (STARTTIMESTAMP instanceof Integer){
                            setCorrectInteger(true);
                        }
                        if (!isDateValid()){
                            //красим в красный
                            jftf.setForeground(Color.red);
                        }else{
                            //иначе красим в зеленый
                            jftf.setForeground(Color.green);
                        }
                    }
                    @Override
                    public void insertUpdate(DocumentEvent e) {                      
                        if (!isDateValid()){
                            //красим в красный
                            jftf.setForeground(Color.red);
                        }else{
                            //иначе красим в зеленый
                            jftf.setForeground(Color.green);
                        }
                        if (STARTTIMESTAMP instanceof Double){
                            setCorrectDouble(false);
                        }else if (STARTTIMESTAMP instanceof Integer){
                            setCorrectInteger(false);
                        }
                    }
                });
            }
        }
    }
