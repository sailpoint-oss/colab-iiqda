package sailpoint.iiqda.core;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class SubstitutingInputStream extends FilterInputStream {

  private Properties substitutions;
  private InputStream iStream;
  private char[] substBuffer;
  private int substIdx=-1;

  private char lookahead;
  private boolean haslookahead=false;
  private List<String> substitutionWarnings;

  public SubstitutingInputStream(Properties substitutions, InputStream f){
    super(f);
    init(substitutions, f);
  }

  private void init(Properties substitutions, InputStream f) {
    System.out
    .println("SubstitutingInputStream.SubstitutingInputStream: ");
    this.iStream=f;
    this.substitutions=substitutions;
    if (this.substitutions==null) this.substitutions=new Properties();
    this.substitutionWarnings=new ArrayList<String>();
  }

  public SubstitutingInputStream(IProject parentProject, String environment,
      InputStream is) {
    super(is);
    IFile f=parentProject.getFile(environment+IIQDAConstants.TARGET_SUFFIX);
    Properties props=new Properties();
    try {
      props.load(f.getContents());
    } catch (IOException | CoreException e) {
      // what should I do here?
    }
    init(props, is);
    // TODO Auto-generated constructor stub
  }

  @Override
  public int read() throws IOException {
    // What we need to do here is:
    // if anything is in the buffer, remove it and return that
    //   else
    // get a char from the stream
    // if it's a '%'
    // get the next char
    // if that's a '%', read bytes until the next '%%'
    // if it's not a '%', put it in lookahead and set useLookahead to true
    // Then look it up in the substitutions
    // if we find it, put the substitution in the buffer
    // otherwise, put the unsubstituted value in the buffer
    // and push it to the substitutionWarnings list

    try {
      if(substIdx!=-1&&substIdx<substBuffer.length) {
        return substBuffer[substIdx++];
      }
      if(iStream==null) throw new IOException("Source InputStream is null");

      int next=-1;
      if (haslookahead) {
        next=lookahead;
        haslookahead=false;
      } else {
        next=iStream.read();
      }
      if(next==-1) return next; // EOS

      char ch=(char)next;
      if(ch!='%') {
        return ch; // not interested yet
      }

      // now we have to check for another %
      char chNext=(char)iStream.read();
      if(chNext!='%') {
        // not our substitution; save the char for next time and return the first char
        haslookahead=true;
        lookahead=chNext;
        return ch;
      }

      StringBuilder lookingFor=new StringBuilder("%%");

      boolean eofSubst=false;

      // read first char
      // until eofSubst:
      //   read second char: if both are '%', write them to the string builder and set eofSubst to true
      //   if not, write the first to the string builder, and move the second to the first 

      char cFirst=(char)iStream.read();
      char cSecond=(char)-1;
      if(cFirst==-1) {
        // trivial check. %% appeared at end of file
        throw new IOException("EOF found looking for substituion string");
      }

      boolean invalidSub=false;

      while(!eofSubst) {
        cSecond=(char)iStream.read();
        if(cSecond==-1) {
          // trivial check. %% appeared at end of file
          throw new IOException("EOF found looking for substituion string");
        }

        if(cFirst=='%' && (cSecond=='%')) {
          lookingFor.append("%%");
          eofSubst=true;
        } else {
          lookingFor.append(cFirst);
          if (isWhitespace(cFirst)||isWhitespace(cSecond)) {
            lookingFor.append(cSecond);
            invalidSub=true;
            eofSubst=true;
          }

          cFirst=cSecond;
        }

      }

      String sLookingFor = lookingFor.toString();
      String substitute=invalidSub?sLookingFor:substitutions.getProperty(sLookingFor);
      if(substitute==null) {
        // Couldn't find this substitution
        substitutionWarnings.add(sLookingFor);
        substitute=sLookingFor;
      }

      substBuffer=(substitute.toCharArray());
      substIdx=1;
      return substBuffer[0];
    } catch (IOException ioe) {
      CorePlugin.logException("SubstitutingInputStream.debug: IOException", ioe);
      throw ioe;
    }
  }

  private boolean isWhitespace(char cSecond) {
    return (cSecond==' '||cSecond=='\t'||cSecond=='\n');
  }

  @Override
  public int available() throws IOException {
    return iStream.available();
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte)c;

    int i = 1;
    try {
      for (; i < len ; i++) {
        c = read();
        if (c == -1) {
          break;
        }
        b[off + i] = (byte)c;
      }
    } catch (IOException ee) {
    }
    return i;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public void close() throws IOException {
    iStream.close();
  }

  @Override
  public boolean markSupported() {
    return false;
  }

  public boolean hasWarnings() {
    return (substitutionWarnings.size()>0);
  }

  public List<String> getWarnings() {
    return substitutionWarnings;
  }

}
