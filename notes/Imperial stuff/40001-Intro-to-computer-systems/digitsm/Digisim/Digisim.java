// Digisim.java
// 
// Simple digital simulator for testing the first year hardware coursework
//
//

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.lang.Math;


public class Digisim extends Frame implements ActionListener
{
    public static final int AND=1;
           static final int OR=2;
           static final int NAND=3;
           static final int NOR=4;
           static final int NOT=5;
           static final int XOR=6;
           static final int XNOR=7;
           
           static final int TRUE=1;
           static final int FALSE=0;
           static final int UNDEFINED = -1;
           
           static final int INPUT = 0;
           static final int OUTPUT = 1;
           static final int INTERNAL = 2;
           static final int DEVICE = 3;
    
    private final int wxSize=600;
            final int wySize=400;
            final int fontSize=14;
            final int borderSize=8;
            final int menuBarSize=25;
            final int maxNodes = 100;
            final int maxGates = 100;
            final int maxFanIn = 10;   // the maximum number of inputs a gate can have
            final int maxInputs = 8;   // the maximum number of inputs the circuit can have
            final int maxOutputs = 8;  // the maximum number of outputs the circuit can have
            final int maxRows = 256;   // the maximum number of rows in a truth table
            final int noErrorMessages = 10;

            int noNodes;
            int noGates;
            
            Gate gateNumber[] = new Gate[maxGates];
            Node nodeNumber[] = new Node[maxNodes];
            ErrorHandler eh = new ErrorHandler();
            TruthTable tt = null;
            int uniqueStudentNumber = -1;
            
       FileDialog f = new FileDialog(this, "Load a Wiring List", FileDialog.LOAD);
       FileDialog s = new FileDialog(this, "Save Report", FileDialog.SAVE);
       String circuitFilename = "none";
       String reportFilename = "myfile.txt";
       String test = "initial";    // this string is used for debugging

       // The Error Handler Class
       //
       // Provides methods for tracking and displaying errors
       public class ErrorHandler
       {   public int noErrorsFound;
           private int [] errorsFound = new int [noErrorMessages];
           private String Error [] = new String [noErrorMessages];
            
           public ErrorHandler() 
           {   noErrorsFound = 0;
               // set upt the error messages
               Error[0] = "Error opening circuit file";
               Error[1] = "<circuit> tag missing";
               Error[2] = "</circuit> tag missing";
               Error[3] = "</inputs> tag missing";
               Error[4] = "</outputs> tag missing";
               Error[5] = "</devices> tag missing";
               Error[6] = "</number> tag missing";
               Error[7] = "Number definition wrong";
               Error[8] = "Error writing the report file";
               Error[9] = "</gates> tag missing";
           }
           
           public void reportError(int no)
           {  if (noErrorsFound<noErrorMessages)
              {  errorsFound[noErrorsFound] = no;
                 noErrorsFound = noErrorsFound + 1;
              }
           }
               
           public void resetErrors()
           {   noErrorsFound = 0;
           }
           
           public void displayErrors(Graphics g)
           {    // this method is to display the errors on the screen
               int j=0;
               int ycoord;
               
               g.drawString("Errors found:",50,75);
               while (j<noErrorsFound)
               {   ycoord = j*(fontSize+2) + 100;
                   g.drawString(Error[errorsFound[j]], 50, ycoord);
                   j=j+1;
               }             
           }
           
           public void writeErrorReport(DataOutputStream op)
           {   // this method is to write the error messages to file
               int j=0;
               
               try
               {   op.write(13);op.write(10);op.write(13);op.write(10);
                   op.writeBytes("Errors found:");
                   op.write(13);op.write(10);op.write(13);op.write(10);
                   while(j<noErrorsFound)
                   {  op.writeBytes(Error[errorsFound[j]]);
                      op.write(13);op.write(10);
                      j=j+1;
                   }
               }
               catch(Exception e)
               {   eh.reportError(8);
               }
           }
       }
       
       // The Parse String Class
       //
       // This class contains methods for separating items of text separated
       // by white spaces.
       //
       // In this implementation text can be letters or digits
       // all other characters are treated as whitespaces
       //
       public class parseString
       {    public String theString;
                   String theHead;
                   
            public parseString(String newString)
            {  theString = newString.toString();
               getTheHead();
            }
            
            private boolean whitespace(char ch)
            {  // for simplicity this implementation treats everythin except
               // digits and letters as whitespaces.        
               if (   (( ch > 47) && (ch < 58))    //digit
                   || (( ch > 64) && (ch < 91))    //UC Letter
                   || (( ch > 96) && (ch < 123))   //LC Letter            
                  )  return false;
              else return true;
            }
           
            private void getTheHead()
            {   StringBuffer theCharacters = new StringBuffer();
                char ch;
                int j;
              
                theCharacters.setLength(0);
                if (theString == null) theHead = null;
                else
                {  j=0;
                   ch = theString.charAt(j);
                   while ((whitespace(ch)) && (j<theString.length()-1))
                   {   j=j+1;
                       ch = theString.charAt(j);
                   }
                   theCharacters.append(ch);
                   if ((whitespace(ch)) && (j==theString.length()-1))
                       theHead = null;  // theString is all whitespaces
                   else
                   {   //get the head into a buffer
                       while ((!whitespace(ch)) && (j<theString.length()-1))
                       { j=j+1;
                         ch = theString.charAt(j);
                         if (!whitespace(ch)) theCharacters.append(ch);
                       }
                       theHead = theCharacters.toString();
                   }
                }
            }
                         
                
            private void beHead()
            {   StringBuffer theCharacters = new StringBuffer();
                char ch;
                int j;
              
                theCharacters.setLength(0);
                if (theString == null) theHead = null;
                else
                {  j=0;
                   ch = theString.charAt(j);
                   while ((whitespace(ch)) && (j<theString.length()-1))
                   {   j=j+1;
                       ch = theString.charAt(j);
                   }
                   if ((whitespace(ch)) && (j==theString.length()-1))
                       theString = null;  // theString is all whitespaces
                   else
                   {   //move past the head into a buffer
                       while ((!whitespace(ch)) && (j<theString.length()-1))
                       { j=j+1;
                         ch = theString.charAt(j);
                       }
                       if (j==theString.length()-1) theString = null;
                       else 
                       {  while (j<theString.length()-1)
                          {  j=j+1;
                             ch = theString.charAt(j);
                             theCharacters.append(ch);
                          }
                          theString = theCharacters.toString();
                       }
                   }
                }
                getTheHead();
            }       
       }
       
       
       //
       // The Truth Table Class
       //
       // This class stores a truth table and allows access to its entries
       //
       
       public class TruthTable
       {   private int table [][] = new int [maxRows][maxInputs + maxOutputs + 2];
           private String headings [] = new String [maxInputs + maxOutputs + 2];
           int inputs;
           int outputs;
           int rows;
           private String tempstring;
           
           public TruthTable(int ip, int op)
           {   // initialise the truth table
               if (ip<=maxInputs) inputs = ip;
               if (op<maxOutputs) outputs = op;
               rows = 2;
               for (int j=1; j<ip; j++) rows=rows*2;
               for (int j=0;j<rows;j=j+1)
               {    int bitstring = j;
                    for (int k=ip-1; k>=0; k--)
                    {   table[j][k] = bitstring % 2;
                        bitstring = bitstring / 2;
                    }
                    for (int k=inputs; k<inputs+outputs+2; k++)
                        table[j][k] = UNDEFINED;
               }
               getNodeNames();
           }
           
           private void getNodeNames()
           {   int inputsFound=0;  
               int outputsFound=0;
               
               for (int j=0;j<noNodes;j=j+1)
               {    if (nodeNumber[j].type == INPUT)
                    {   if (inputsFound<inputs) headings[inputsFound] = nodeNumber[j].Name;
                        inputsFound = inputsFound + 1;
                    }
                    if (nodeNumber[j].type == OUTPUT)
                    {  if (outputsFound<outputs) headings[inputs+outputsFound] = nodeNumber[j].Name;
                        outputsFound = outputsFound + 1;
                     }
                 }
                 headings[inputs+outputs+1] = "Cycles";
                 headings[inputs+outputs] = "Expected";
           }
           
           public int getInput(int row, int inputIndex)
           {   return table[row][inputIndex];
           }
           
           public int getOutput(int row, int outputIndex)
           {   return table[row][outputIndex + inputs];
           }
           
           public int getRows()
           {   return rows;
           }
           
           public void setOutput(int row, int outputIndex, int state)
           {   table[row][outputIndex + inputs] = state;
           }
           
           public void setExpectedOutput(int row, int state)
           {   table[row][outputs + inputs] = state;
           }
           
           public void setCycleCount(int row, int count)
           {   table[row][outputs + inputs + 1] = count;
           }
           
           public void displayTruthTable(Graphics g)
           {   // method to display the truth table on the screen
               int xcoord,ycoord;
               
               ycoord = 100; xcoord = 50;
               for (int k=0; k<inputs+outputs; k++)
               {  xcoord = 50 + k*(4*fontSize);
                  g.drawString(headings[k], xcoord, ycoord);
               }
               xcoord += 4*fontSize;
               g.drawString(headings[inputs+outputs],xcoord,ycoord);
               xcoord += 6*fontSize;
               g.drawString(headings[inputs+outputs+1],xcoord,ycoord);
               for (int j=0; j<rows; j=j+1)
               {  ycoord = 115 + j*(fontSize+2);
                  for(int k=0; k<inputs;k=k+1)
                  {   xcoord = 50 + k*(4*fontSize);
                      if (table[j][k] == TRUE)  g.drawString("1",xcoord,ycoord);
                      if (table[j][k] == FALSE) g.drawString("0",xcoord,ycoord);
                      if (table[j][k] == UNDEFINED)  g.drawString("u1",xcoord,ycoord);
                        
                  }
                  for(int k=inputs; k<inputs+outputs+1; k=k+1)  // display outputs and expected outputs
                  {   xcoord = 50 + k*(4*fontSize);
                      if (table[j][k] == TRUE) g.drawString("1",xcoord,ycoord) ;
                      if (table[j][k] == FALSE)g.drawString("0",xcoord,ycoord);
                      if (table[j][k] == UNDEFINED)g.drawString("u",xcoord,ycoord);
                  }  
                  xcoord = xcoord + 6*fontSize;
                  if (table[j][inputs+outputs+1] == UNDEFINED)
                      g.drawString("Unstable", xcoord, ycoord);
                  else 
                      g.drawString(tempstring.valueOf(table[j][inputs+outputs+1]),xcoord,ycoord);
               }
           }
           
           public void writeTruthTable(DataOutputStream op)
           {    try
                {    op.write(13);op.write(10);op.write(13);op.write(10);
                     op.writeBytes("Truth Table");op.write(13);op.write(10);op.write(13);op.write(10);
                     for (int k=0; k<inputs+outputs; k++)
                     {  op.writeBytes(headings[k]);op.write(9);
                     }
                     op.writeBytes(headings[inputs+outputs]);op.write(9);
                     op.writeBytes(headings[inputs+outputs+1]);op.write(9);
                     op.write(13);op.write(10);op.write(13);op.write(10);
                     for (int j=0; j<rows; j=j+1)
                     {  for(int k=0; k<inputs;k=k+1)
                        {   if (table[j][k] == TRUE)  op.writeBytes("1");
                            if (table[j][k] == FALSE)  op.writeBytes("0");
                            if (table[j][k] == UNDEFINED)  op.writeBytes("u");
                            op.write(9);
                        }
                        for(int k=inputs; k<inputs+outputs+1;k=k+1)
                        {   if (table[j][k] == TRUE)  op.writeBytes("1");
                            if (table[j][k] == FALSE)  op.writeBytes("0");
                            if (table[j][k] == UNDEFINED)  op.writeBytes("u");
                            op.write(9);
                        }
                        op.write(9);
                        if (table[j][inputs+outputs+1] == UNDEFINED)
                               op.writeBytes("Unstable");
                        else op.writeBytes(tempstring.valueOf(table[j][inputs+outputs+1]));
                        op.write(13);op.write(10);
                     }
                }
                catch (Exception e)
                {    eh.reportError(8);
                }
           }
       }

    // The Gate Class
    // 
    // This class handles AND, NAND and NOT gates
    // The current implementation only deals with up to two inputs
    //   

    public class Gate
    {   private int type;
                int noInputs;
                int [] ip = new int [maxFanIn];
                int op;
                int result, j;
                int noFalse, noTrue, noUndefined;
                String [] ipName = new String[maxFanIn];
                String opName;

        private void CountInputs()
        {    noFalse=0;
             noTrue=0;
             noUndefined=0;
             for (j=0; j<noInputs; j++)
             {   
                 if (nodeNumber[ip[j]].GetPresentState() == TRUE) noTrue++ ;
                 if (nodeNumber[ip[j]].GetPresentState() == FALSE) noFalse++ ;
                 if (nodeNumber[ip[j]].GetPresentState() == UNDEFINED) noUndefined++ ;
             }
        }

        // Constructor for an invertor        
        public Gate(String outputNode, String inputNode )
        {   type = NOT;
            ip[0] = findNodeIndex(inputNode);
            op = findNodeIndex(outputNode);
            ipName[0] = inputNode;
            opName = outputNode;
            noInputs = 1;
        }
        
        // Constructor for a 2 input gate
        public Gate(int gateType, String outputNode, String inputNode1, String inputNode2)
        {  type = gateType;
           ip[0] = findNodeIndex(inputNode1);
           ip[1] = findNodeIndex(inputNode2);
           op = findNodeIndex(outputNode);
           ipName[0] = inputNode1;
           ipName[1] = inputNode2;
           opName = outputNode;
           noInputs = 2;
         }
 
        public int findNodeIndex(String nodeName)
        {   for (int j=0; j< noNodes; j++)
               if (nodeNumber[j].Name.equals(nodeName)) return j;
            return j;   // there must be a matching node
        }
        
        public void Update()
        {          
            CountInputs();
                        
            if ((type==AND) || (type==NAND))
            {  result = TRUE;            // work out the AND condition here
               if (noFalse != 0) result = FALSE;
               else if (noUndefined !=0) result = UNDEFINED;
            }
            
            if ((type==OR) || (type==NOR))
            {  result = FALSE;          // work out the OR condition here
               if (noTrue != 0) result = TRUE;
               else if (noUndefined != 0) result = UNDEFINED;
            }

            if ((type==XOR) || (type==XNOR))
            {  result = FALSE;          // work out the OR condition here
               if (noUndefined != 0) result = UNDEFINED;
               else if (noTrue == noFalse) result = TRUE;
            }

            if (type == NOT) result = nodeNumber[ip[0]].GetPresentState();
           
            if ((type == NAND) || (type == NOR) || (type == NOT) || (type == XNOR))
            {  if (result==TRUE) result = FALSE;
               else if (result==FALSE) result = TRUE;
               // undefined inverts to undefined
            } 
            nodeNumber[op].setNextState(result);
        }
    }

    // The Node Class
    // This class handles nodes (or wires) in the wiring list
    // It keeps a value for the present and next state of the node
    //
    public class Node
    {  private int presentState;
               int nextState;
               int type;
               String Name;
               
       public Node(String nodeName, int nodeType)
       {   presentState = UNDEFINED;
           nextState = UNDEFINED;
           type = nodeType;
           Name = nodeName.toString();
       }
       
       public void setNodeType(int nodeType)
       {    type = nodeType;
       }
       
       public void Update()
       {   presentState = nextState;
       }
        
       public void setNextState(int value)
       {   if (!Name.equals("OPEN"))     // can't set an open node to anything it is always undefined
                if ( (value <= 1 ) && (value >= -1) ) nextState = value;
           // some error handling here would be good
       }
       
       public int GetPresentState()
       {  return presentState;
       }
       
       public boolean Changed()
       {   if (presentState == nextState) return false;
           else return true;
       }
    }

    public static void main(String args[])
    {    
        Digisim u = new Digisim();
    }

    private void SetUpMenuBar()
    {  setFont(new Font("Times", Font.BOLD, fontSize));
       MenuBar me = new MenuBar();
       setMenuBar(me);
       Menu ItemFile = new Menu("File");
       me.add(ItemFile);
       ItemFile.add(new MenuItem("Open Circuit File"));
       ItemFile.add(new MenuItem("Save Report"));
       ItemFile.add(new MenuItem("Quit"));
       ItemFile.addActionListener(this);

    }
 
    public Digisim()
    {  setTitle("First Year Hardware Coursework - Digisim");
       setSize(wxSize,wySize);
       setBackground(Color.lightGray);
       setFont(new Font("Times", Font.PLAIN, fontSize));
       setVisible(true);
       SetUpMenuBar();
       Close c = new Close();
       addWindowListener(c);
       Mouse m = new Mouse();
       repaint();
    }
    
    
    public void writeReportFile(String Filename)
    {  DataOutputStream op;
       String tempstring = "none";
       
        try
        {   op = new DataOutputStream(new FileOutputStream(Filename));
            op.writeBytes("Report on the design of circuit number ");
            op.writeBytes(tempstring.valueOf(uniqueStudentNumber));
            op.write(13);op.write(10);op.write(13);op.write(10);
            op.writeBytes("Wires");op.write(13);op.write(10);
            for (int j=0; j<noNodes; j++)
            {  op.writeBytes(nodeNumber[j].Name.toString());
               op.write(9);
               if (nodeNumber[j].type == INPUT) op.writeBytes("Input");
               if (nodeNumber[j].type == OUTPUT) op.writeBytes("Output");
               if (nodeNumber[j].type == INTERNAL) op.writeBytes("Internal");
               op.write(13);op.write(10);
            }
            op.write(13);op.write(10);op.write(13);op.write(10);
            op.writeBytes("Gates");op.write(13);op.write(10);
            for (int j=0; j<noGates; j++)
            {  if (gateNumber[j].type == NOT) op.writeBytes("NOT");
               if (gateNumber[j].type == AND) op.writeBytes("AND");
               if (gateNumber[j].type == NAND) op.writeBytes("NAND");
               if (gateNumber[j].type == OR) op.writeBytes("OR");
               if (gateNumber[j].type == NOR) op.writeBytes("NOR");
               if (gateNumber[j].type == XOR) op.writeBytes("XOR");
               if (gateNumber[j].type == XNOR) op.writeBytes("XNOR");
               op.write(9);
               op.writeBytes(gateNumber[j].opName.toString());
               op.write(9);
               op.writeBytes(gateNumber[j].ipName[0].toString());
               op.write(9);
               if (gateNumber[j].type != NOT)
               {   op.writeBytes(gateNumber[j].ipName[1].toString());
                   op.write(9);
               }
               op.write(13);op.write(10);
            }
            if (tt!=null) tt.writeTruthTable(op);
            if (eh.noErrorsFound>0) eh.writeErrorReport(op);
            op.close();
        }
        catch (Exception e)
        {
        } 
    }
    
    private void findExpectedOutputs(int circuitNumber)
    {   int D0, D1, D2, D3, temp;
        int A, B, R;
        
        if (circuitNumber == -1) return;
        temp = circuitNumber;
        D0 = temp % 4;
        temp = temp / 4; D1 = temp % 4;
        temp = temp / 4; D2 = temp % 4;
        temp = temp / 4; D3 = temp % 4;
        for (int row = 0; row<4; row++ )  // for the first four rows C1=0, C0=0
        {   A =  tt.getInput(row,2);
            B = tt.getInput(row,3);
            R = -1;
            if (D3 == 0) R = (A+B)%2;   // A eor B
            if (D3 == 1) R = 1 - (A+B)%2; // not (A eor B)
            if (D3 == 2) R = 1;
            if (D3 == 3) R = 0;
            tt.setExpectedOutput(row, R);
        }
        for (int row = 4; row<8; row++ )  // for the next four rows C1=0, C0=1
        {   A =  tt.getInput(row,2);
            B = tt.getInput(row,3);
            R = -1;
            if (D2 == 0) R = A;   // A 
            if (D2 == 1) R = 1 - A; // not A
            if (D2 == 2) R = 1-B;
            if (D2 == 3) R = B;
            tt.setExpectedOutput(row, R);
        }
        for (int row = 8; row<12; row++ )  // for the next four rows C1=1, C0=0
        {   A =  tt.getInput(row,2);
            B = tt.getInput(row,3);
            R = -1;
            if (D1 == 0) R = A*B;   // A.B 
            if (D1 == 1) R = A*(1-B); // notA . B
            if (D1 == 2) R = (1-A)*(1-B);
            if (D1 == 3) R = (1-A)*B;
            tt.setExpectedOutput(row, R);
        }
        for (int row = 12; row<16; row++ )  // for the next four rows C1=1, C0=1
        {   A =  tt.getInput(row,2);
            B = tt.getInput(row,3);
            R = -1;
            if (D0 == 0) R = A+B - A*B;   // A+B
            if (D0 == 1) R = (1-A) + B - (1-A)*B; // notA+B
            if (D0 == 2) R = A+(1-B) - A*(1-B); // A+notB;
            if (D0 == 3) R = (1-A)+(1-B) - (1-A)*(1-B); // notA+notB;
            tt.setExpectedOutput(row, R);
        }
       
    }
    
    private int findSteadyState()
    {   // finds the stead state, if any
        
        int j=0;
        boolean steady = false;
        while ((!steady) && (j<=noGates))
        {   for (int k=0; k<noGates ; k=k+1)
               gateNumber[k].Update();
            steady = true;
            for (int k=0; k<noNodes; k=k+1)
            {   if (nodeNumber[k].Changed()) 
                {   steady = false;
                    nodeNumber[k].Update();
                }
            }
            j=j+1;
        }
        if (j>noGates) return -1;
        else return j;        
    }

    private void generateTruthTable()
    {   // generates a truth table
        int [] inputNodes = new int[maxInputs];
        int [] outputNodes = new int[maxOutputs];
        int noInputs=0;
        int noOutputs=0;
        int noCycles=0;
        
        // first of all find the input and the output nodes
        for (int j=0;j<noNodes;j=j+1)
        {    if (nodeNumber[j].type == INPUT)
             {   inputNodes[noInputs] = j;
                 noInputs = noInputs+1;
             }
             if (nodeNumber[j].type == OUTPUT)
             {   outputNodes[noOutputs] = j;
                 noOutputs = noOutputs+1;
             }
        }
        tt = new TruthTable(noInputs,noOutputs);
        // now cycle through the inputs calculating the outputs
        for (int j=0; j<tt.getRows() ; j=j+1)
        {   for (int k=0; k<noInputs; k=k+1) 
            {  nodeNumber[inputNodes[k]].setNextState(tt.getInput(j,k));
               nodeNumber[inputNodes[k]].Update();
            }
            noCycles = findSteadyState();
            tt.setCycleCount(j, noCycles);
            if (noCycles != UNDEFINED) 
            {   for (int k=0; k<noOutputs; k++)
                {   tt.setOutput(j,k,nodeNumber[outputNodes[k]].GetPresentState());
                }
            }      
        }
        findExpectedOutputs(uniqueStudentNumber);
    }
   
    private void insertNode(String nodeName, int nodeType)
    {   // first check to see if the node exists
        boolean nodeExists = false;
        for (int j=0; j<noNodes; j++)
          if (nodeName.equals(nodeNumber[j].Name))
          {     nodeExists = true;
                // correct node type (if devices are placed before inputs and outputs
                if((nodeType == INPUT) || (nodeType == OUTPUT))
                {  nodeNumber[j].setNodeType(nodeType);
                }
          }
        if (!nodeExists)
        {  nodeNumber[noNodes] = new Node(nodeName,nodeType);
           noNodes = noNodes+1;
        }
    }


    private void processSN74(String pinConnections)
    {    parseString thePins;
         String deviceName;
         String [] thePinNames = new String[20];
         int gateType;
         
         thePins = new parseString(pinConnections);
         deviceName = thePins.theHead;
         thePins.beHead();   //remove the device name
         for (int j =1;j<15; j++)  // numbering to follow the standard
         {   thePinNames[j] = new String(thePins.theHead);
             thePins.beHead();
             //if ( ! thePinNames[j].equals("OPEN"))
                   insertNode(thePinNames[j], INTERNAL);  
         }
         
         if (deviceName.equals("SN7406"))
         {  gateNumber[noGates] = new Gate(thePinNames[2], thePinNames[1]);
            noGates = noGates+1;
            gateNumber[noGates] = new Gate(thePinNames[4], thePinNames[3]);
            noGates = noGates+1;
            gateNumber[noGates] = new Gate(thePinNames[6], thePinNames[5]);
            noGates = noGates+1;
            gateNumber[noGates] = new Gate(thePinNames[8], thePinNames[9]);
            noGates = noGates+1;
            gateNumber[noGates] = new Gate(thePinNames[10], thePinNames[11]);
            noGates = noGates+1;
            gateNumber[noGates] = new Gate(thePinNames[12], thePinNames[13]);
            noGates = noGates+1;
         }
         else
         {   if (deviceName.equals("SN7400")) gateType = NAND;
             else gateType = AND;
             gateNumber[noGates] = new Gate(gateType, thePinNames[3], thePinNames[1], thePinNames[2]);
             noGates = noGates+1;
             gateNumber[noGates] = new Gate(gateType, thePinNames[6], thePinNames[4], thePinNames[5]);
             noGates = noGates+1;
             gateNumber[noGates] = new Gate(gateType, thePinNames[8], thePinNames[9], thePinNames[10]);
             noGates = noGates+1;
             gateNumber[noGates] = new Gate(gateType, thePinNames[11], thePinNames[12], thePinNames[13]);
             noGates = noGates+1;
        }
    }

    private void processInverter(String pinConnections)
    {    parseString thePins;
         String deviceName;
         String [] thePinNames = new String[20];
         int gateType;

         thePins = new parseString(pinConnections);
         deviceName = thePins.theHead;
         thePins.beHead();   //remove the device name
         for (int j =1;j<3; j++)  // numbering to follow the standard
         {   thePinNames[j] = new String(thePins.theHead);
             thePins.beHead();
             //if ( ! thePinNames[j].equals("OPEN"))
                   insertNode(thePinNames[j], INTERNAL);  
         }
         gateNumber[noGates] = new Gate(thePinNames[1], thePinNames[2]);
         noGates = noGates+1;
    }

    private void processTwoInput(String pinConnections)
    {    parseString thePins;
         String deviceName;
         String [] thePinNames = new String[20];
         int gateType;

         thePins = new parseString(pinConnections);
         deviceName = thePins.theHead;
         thePins.beHead();   //remove the device name
         for (int j =1;j<4; j++)  // numbering to follow the standard
         {   thePinNames[j] = new String(thePins.theHead);
             
             thePins.beHead();
             //if ( ! thePinNames[j].equals("OPEN"))
                   insertNode(thePinNames[j], INTERNAL);  
         }
         gateType = AND;
         if (deviceName.equals("NAND")) gateType = NAND;
         if (deviceName.equals("OR")) gateType = OR;
         if (deviceName.equals("NOR")) gateType = NOR;
         if (deviceName.equals("XOR")) gateType = XOR;
         if (deviceName.equals("XNOR")) gateType = XNOR;

         gateNumber[noGates] = new Gate(gateType, thePinNames[1], thePinNames[2], thePinNames[3]);
         noGates = noGates+1;
    }

    private void processParameterString(String inputList, int stringType)
    {   parseString nodeList;
        
        nodeList = new parseString(inputList);
        
        while (!(nodeList.theHead == null))
        {   insertNode(nodeList.theHead, stringType);
            nodeList.beHead();
        }
    }

    private int processDeviceString(String deviceString)
    {   parseString theDevices;
        
        theDevices = new parseString(deviceString);
        while (theDevices.theHead != null)
        {  if (theDevices.theHead.equals("SN7400")
              || (theDevices.theHead.equals("SN7406"))
              || (theDevices.theHead.equals("SN7408"))) processSN74(theDevices.theString);
           else return 0;
           for( int j=0; j<15; j++) theDevices.beHead();  // remove the data just processed
        }
        return 1;
    }

    private int processGateString(String gateString)
    {   parseString theGates;
        
        theGates = new parseString(gateString);
        while (theGates.theHead != null)
        {  if (theGates.theHead.equals("INVERTER")) 
           {  processInverter(theGates.theString);
              for( int j=0; j<3; j++) theGates.beHead();  // remove the data just processed
           }
           else if ((theGates.theHead.equals("AND")) || (theGates.theHead.equals("NAND")) || (theGates.theHead.equals("OR"))
                       || (theGates.theHead.equals("NOR")) || (theGates.theHead.equals("XOR")) || (theGates.theHead.equals("XNOR"))) 
                {   processTwoInput(theGates.theString);
                    for( int j=0; j<4; j++) theGates.beHead();  // remove the data just processed
                }
                else return 0;
        }
        return 1;
    }

    private void processNumberString(String deviceString)
    {   parseString theNumber;
        int theValue;
        
        theNumber = new parseString(deviceString);
        if (theNumber.theHead != null)
        {  theValue = 0;
           for (int j=0;j<theNumber.theHead.length(); j=j+1)
              theValue = 10*theValue + (int)theNumber.theHead.charAt(j) - 48;
           if ((theValue>=0) && (theValue<256))
           {   uniqueStudentNumber = theValue;
               return;       // unique student number is valid
           }
        }
        eh.reportError(7);
    }

    private void parseCircuitFile(String Filename)
    {   // variables to indicate progress
        boolean circuitFound=false;
        boolean endCircuitFound=false;
        
        // bug fix to remove nodes/gates from previous files 
        // thanks to Steven Lovegrove, James Huggett and Laurence Pyke
        noNodes = 0;
        noGates = 0;
        
        StringBuffer characterBuffer = new StringBuffer();
        int ch;
        int j=0;
 //       int returnCode;
        String currentTag;
        String matchingTag;
        String parameterString;
        
        try
        {  FileInputStream ip = new FileInputStream(Filename);
           eh.resetErrors();    // clear the errors
           ch = ip.read();

           while ((endCircuitFound == false) && (ch != -1))
           {   characterBuffer.setLength(0);  //clear the buffer
               // get the next tag
               while ((ch!=60)&& (ch != -1)) ch = ip.read(); // search for <
               characterBuffer.append((char) ch);
               while ((ch!=62)&& (ch != -1)) // get the tag text
               {  ch = ip.read();  
                  characterBuffer.append((char) ch);
                  //System.out.println(ch);
               }
               currentTag = characterBuffer.toString();
               if (currentTag.equals("<circuit>"))
               {  circuitFound = true;
               }
               else if (currentTag.equals("</circuit>"))
               { endCircuitFound = true;
               }
               else if (currentTag.equals("<inputs>"))
               {    characterBuffer.setLength(0);   // get the string containing the variable names
                    while ((ch!=60)&& (ch != 255))
                    {   ch =  ip.read();
                        characterBuffer.append((char) ch);
                    }
                    parameterString = characterBuffer.toString();
                    characterBuffer.setLength(0);
                    characterBuffer.append((char) ch);    // get matching closing tag
                    for (j=0;j<8;j++) 
                    {  ch =  ip.read();
                       characterBuffer.append((char) ch);
                    }
                    matchingTag = characterBuffer.toString();
                    if (matchingTag.equals("</inputs>"))
                    {   processParameterString(parameterString, INPUT);
                    }
                    else eh.reportError(3);    // report matching error
               }
               else if (currentTag.equals("<outputs>"))
               {    characterBuffer.setLength(0);   // get the string containing the variable names
                    while ((ch!=60)&& (ch != 255))
                    {   ch =  ip.read();
                        characterBuffer.append((char) ch);
                    }
                    parameterString = characterBuffer.toString();
                    characterBuffer.setLength(0);
                    characterBuffer.append((char) ch);    // get matching closing tag
                    for (j=0;j<9;j++) 
                    {  ch =  ip.read();
                       characterBuffer.append((char)ch);
                    }
                    matchingTag = characterBuffer.toString();
                    if (matchingTag.equals("</outputs>"))
                    {   processParameterString(parameterString, OUTPUT);
                    }
                    else eh.reportError(4);    // return indicating matching error
               }
               else if (currentTag.equals("<devices>"))
               {    characterBuffer.setLength(0);   // get the string containing the variable names
                    while ((ch!=60)&& (ch != -1))
                    {   ch =  ip.read();
                        characterBuffer.append((char) ch);
                    }
                    parameterString = characterBuffer.toString();
                    characterBuffer.setLength(0);
                    characterBuffer.append((char) ch);    // get matching closing tag
                    for (j=0;j<9;j++) 
                    {  ch =  ip.read();
                       characterBuffer.append((char) ch);
                    }
                    matchingTag = characterBuffer.toString();
                    if (matchingTag.equals("</devices>"))
                    {   processDeviceString(parameterString);
                    }
                    else eh.reportError(5);    // report matching error
               }
               else if (currentTag.equals("<number>"))
               {    characterBuffer.setLength(0);   // get the string containing the unique number
                    while ((ch!=60)&& (ch != -1))
                    {   ch =  ip.read();
                        characterBuffer.append((char) ch);
                    }
                    parameterString = characterBuffer.toString();
                    characterBuffer.setLength(0);
                    characterBuffer.append((char) ch);    // get matching closing tag
                    for (j=0;j<8;j++) 
                    {  ch =  ip.read();
                       characterBuffer.append((char) ch);
                    }
                    matchingTag = characterBuffer.toString();
                    if (matchingTag.equals("</number>")) processNumberString(parameterString);
                    else eh.reportError(6);    // return indicating matching error
               }    
               else if (currentTag.equals("<gates>"))
               {    characterBuffer.setLength(0);   // get the string containing the gate definitions
                    while ((ch!=60)&& (ch != -1))
                    {   ch =  ip.read();
                        characterBuffer.append((char) ch);
                    }
                    parameterString = characterBuffer.toString();
                    characterBuffer.setLength(0);
                    characterBuffer.append((char) ch);    // get matching closing tag
                    for (j=0;j<7;j++) 
                    {  ch =  ip.read();
                       characterBuffer.append((char) ch);
                    }
                    matchingTag = characterBuffer.toString();
                    if (matchingTag.equals("</gates>")) processGateString(parameterString);
                    else eh.reportError(9);    // return indicating matching error
               }          
           }
           if (!circuitFound) eh.reportError(1);
           if (!endCircuitFound) eh.reportError(2);
        }
        catch (IOException e)
        {   // catch IO Exception
            // end of file, give error message on progress
            // all other exceptions
            eh.reportError(0);
        }
    }
    
    public void actionPerformed(ActionEvent e)
    {   String ip;
        String command = e.getActionCommand();
        
        // Actions performed on menu commands
        if (command.equals("Quit"))
        {  dispose();
           System.exit(0);
        }  
        if (command.equals("Open Circuit File"))
        {  f.setVisible(true);
           eh.resetErrors();
           ip = f.getDirectory(); // check for null
           if (ip!=null) circuitFilename = ip;
           ip = f.getFile();  // check for null
           if (ip!=null) circuitFilename += ip;
           if (ip==null) eh.reportError(0);
           else 
           {    parseCircuitFile(circuitFilename);
                if (eh.noErrorsFound == 0)  generateTruthTable();
           }
           repaint();
        }
        
        if (command.equals("Save Report"))
        {  s.setVisible(true);
           ip = s.getDirectory();     // check for null
           if (ip!=null) reportFilename = ip;
           ip = s.getFile();         // check for null
           if (ip!=null) reportFilename += ip;
           if (ip==null) eh.reportError(8);
           else writeReportFile(reportFilename);
           repaint();
        }

    }

    
    public class Close extends WindowAdapter
    {  public void windowClosing(WindowEvent e)
       {   dispose();
           System.exit(0);
       }
    }

    public class Mouse implements MouseListener
    {	int x, y, button;

        public Mouse()
        {   addMouseListener(this);
        }

        public void mouseClicked(MouseEvent e)
        {   x = e.getX();
            y = e.getY();
            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 ) button=1;
            if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0 ) button=2;
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 ) button=2;

            repaint();
        }
        
        public void mousePressed(MouseEvent e)
        {
        }
        public void mouseReleased(MouseEvent e)
        {
        }
        public void mouseEntered(MouseEvent e)
        {
        }
        public void mouseExited(MouseEvent e)
        {
        }
    }

    public void paint(Graphics g)
    {  if (eh.noErrorsFound>0) eh.displayErrors(g);
       else if (tt != null) 
       {   g.drawString("The circuit is syntactically correct. The truth table is:",20,75);
           tt.displayTruthTable(g);
       }
       else g.drawString("No circuit file loaded", 20, 75);
           
    }
}
