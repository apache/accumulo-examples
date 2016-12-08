package org.apache.accumulo.examples.cli;

import com.beust.jcommander.Parameter;

public class ScannerOpts {
  @Parameter(names = "--scanBatchSize", description = "the number of key-values to pull during a scan")
  public int scanBatchSize = 1000;
}
