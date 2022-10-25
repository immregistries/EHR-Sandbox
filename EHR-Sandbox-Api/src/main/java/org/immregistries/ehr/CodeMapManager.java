package org.immregistries.ehr;

import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.CodeMapBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class CodeMapManager {
  CodeMapBuilder builder = CodeMapBuilder.INSTANCE;
  CodeMap codeMap;

  public CodeMapManager() {
    InputStream is = this.getClass().getResourceAsStream("/Compiled.xml");
    if (is == null) {
      System.err.println("Unable to find Compiled.xml!");
    }
    codeMap = builder.getCodeMap(is);
  }

  public CodeMap getCodeMap() {
    return this.codeMap;
  }

}