package burp;
import java.awt.Component;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;


public class BurpExtender implements IBurpExtender, ITab, IProxyListener, IHttpListener
{
    public String ExtensionName =  "AES Killer";
    public String TabName = "AES Killer";
    
    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    
    public MainPanel myPanel;
    
    public Cipher cipher;
    public IvParameterSpec iv_param;
    public SecretKey sec_key;
    public String reqURL = null;
    public String reqParameter = null;
    public String resPrarameter = null;
    public Boolean decResponse = false;
    public Boolean isOffusicated = false;
    public String[] offusicatedChar = null;
    public String[] replaceWithChar = null;
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        stdout = new PrintWriter(callbacks.getStdout(), true);
        this.callbacks.setExtensionName(this.ExtensionName);
        
        myPanel = new MainPanel(this);
        this.callbacks.addSuiteTab(this);
        this.stdout.println("AES Killer Installed");
    }

    @Override
    public String getTabCaption() {
        return this.TabName;
    }

    @Override
    public Component getUiComponent() {
        return this.myPanel;
    }

    @Override
    public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
        if (messageIsRequest){
            IHttpRequestResponse messageInfo = message.getMessageInfo();
            IRequestInfo reqInfo = helpers.analyzeRequest(messageInfo);
            List headers = reqInfo.getHeaders();
            String request = new String(messageInfo.getRequest());
            String URL = new String(reqInfo.getUrl().toString());
            if (URL.contains(this.reqURL)){
                String messageBody = request.substring(reqInfo.getBodyOffset());                
                if ( !this.reqParameter.equals("") && messageBody.startsWith(this.reqParameter)){
                    String arr[] = messageBody.split(this.reqParameter);
                    messageBody = arr[1].substring(0, arr[1].length()-1);
                }
                messageBody = removeNull(messageBody);
                if(this.isOffusicated){messageBody = removeOff(messageBody);}
                messageBody = doDecrypt(messageBody);
                byte[] updateMessage = helpers.buildHttpMessage(headers, messageBody.getBytes());
                messageInfo.setRequest(updateMessage);
            }
            
        }
        else {
            
            if (this.decResponse != true){
                return;
            }
            IHttpRequestResponse messageInfo = message.getMessageInfo();
            IRequestInfo reqInfo = helpers.analyzeRequest(messageInfo);
            String URL = new String(reqInfo.getUrl().toString());
            if (URL.contains(this.reqURL)){
                IResponseInfo resInfo = helpers.analyzeResponse(messageInfo.getResponse());
                List headers = resInfo.getHeaders();
                String response = new String(messageInfo.getResponse());
                String params = new String(response.substring(resInfo.getBodyOffset()));
                try{
                    params = doDecrypt(params);
                    byte[] updateMessage = helpers.buildHttpMessage(headers, params.getBytes());
                    messageInfo.setResponse(updateMessage);
                }
                catch (Exception ex) {
                    stdout.println( params.length() + ": Exception Here");
                }
            }
        }
    }
    
    public String doEncrypt(String paramString){
        try{
            cipher.init(1, sec_key ,iv_param);
            paramString = new String (Base64.getEncoder().encodeToString(cipher.doFinal(paramString.getBytes())));
            return paramString;
        }catch(Exception ex){
            return null;
        }
    }
    
    public String doDecrypt(String paramString){
        try{
            cipher.init(2, sec_key ,iv_param);
            paramString = new String (cipher.doFinal(Base64.getDecoder().decode(paramString)));
            return paramString;
        }catch(Exception ex){
            return null;
        }
    }
    
    public String removeNull(String paramString){
        if (paramString != null) {
          return paramString.replace("%0A", "").replace("%2C","");
        }
        return null;
    }
    
    public String removeOff(String paramString)
    {
        if (paramString != null) {
          for(int i =0; i< this.offusicatedChar.length; i++){
              paramString = paramString.replace(this.replaceWithChar[i], this.offusicatedChar[i]);
          }
//          return paramString.replace("-", "+").replace("_", "/").replace(",", "=");
          return paramString;
        }
        return null;
    }
    
    public String doOff(String paramString)
    {
        if (paramString != null) {
          for(int i =0; i< this.offusicatedChar.length; i++){
              paramString = paramString.replace(this.offusicatedChar[i], this.replaceWithChar[i]);
          }
//          return paramString.replace("+", "-").replace("/", "_").replace("=", ",");
          return paramString;
        }
        return null;
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (messageIsRequest){
            IRequestInfo reqInfo = helpers.analyzeRequest(messageInfo);
            List headers = reqInfo.getHeaders();
            String request = new String(messageInfo.getRequest());
            String URL = new String(reqInfo.getUrl().toString());
            if (URL.contains(this.reqURL)){
                String messageBody = request.substring(reqInfo.getBodyOffset());                
                if ( !this.reqParameter.equals("") && messageBody.startsWith(this.reqParameter)){
                    String arr[] = messageBody.split(this.reqParameter);
                    messageBody = arr[1].substring(0, arr[1].length()-1);
                }
                messageBody = removeNull(messageBody);
                if(this.isOffusicated){messageBody = removeOff(messageBody);}
                messageBody = doDecrypt(messageBody);
                byte[] updateMessage = helpers.buildHttpMessage(headers, messageBody.getBytes());
                messageInfo.setRequest(updateMessage);
            }
            
        }
        else {
            
            if (this.decResponse != true){
                return;
            }
            IRequestInfo reqInfo = helpers.analyzeRequest(messageInfo);
            String URL = new String(reqInfo.getUrl().toString());
            if (URL.contains(this.reqURL)){
                IResponseInfo resInfo = helpers.analyzeResponse(messageInfo.getResponse());
                List headers = resInfo.getHeaders();
                String response = new String(messageInfo.getResponse());
                String params = new String(response.substring(resInfo.getBodyOffset()));
                try{
                    params = doDecrypt(params);
                    byte[] updateMessage = helpers.buildHttpMessage(headers, params.getBytes());
                    messageInfo.setResponse(updateMessage);
                }
                catch (Exception ex) {
                    stdout.println( params.length() + ": Exception Here");
                }
            }
        }
    }
}
