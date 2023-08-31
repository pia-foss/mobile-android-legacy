package main

import (
  "flag"
  "os"
  "os/exec"
  "fmt"
  "log"
)

var strBranch string
var syncPtr = flag.Bool("sync", false, "Sync subrepositories")
var branchPtr = flag.String("branch", "", "Switch git branch")

func main() {
  flag.Parse()
  strBranch = *branchPtr

  flagset := make(map[string]bool)
  flag.Visit(func(f *flag.Flag) { flagset[f.Name]=true } )

  if len(os.Args) < 2 {
    fmt.Println("At least one parameter is required. \n\nAvailable parameters:")
    flag.PrintDefaults()
    os.Exit(1)
  }
  
  if (flagset["sync"]) {
      runCommand("git", []string{"status"});

      if (len(strBranch) > 0) {
        //change branch
        runCommand("git", []string{"checkout", strBranch});
      }

      syncRepos();
  }
}

func syncRepos() {
  runCommand("git", []string{"submodule", "sync", "--recursive"});
  runCommand("git", []string{"submodule", "update", "--init", "--recursive"});
}

func runCommand(command string, params []string) {
  cmd := exec.Command(command, params...)
  cmd.Stdout = os.Stdout
  cmd.Stderr = os.Stderr
  err := cmd.Run()
  if err != nil {
    log.Fatalf("cmd.Run() failed with %s\n", err)
  }
}