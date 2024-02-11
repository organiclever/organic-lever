(ns market-sentinel.utils.shell)

(defn exec-sh-cmd
  "exec-sh-cmd executes a shell command and returns the output or error. The main difference with built in sh command is that it is able to execute full command with arguments."
  [command]
  (let [process   (.exec (Runtime/getRuntime) command)
        exit-code (.waitFor process)
        output    (slurp (.getInputStream process))
        error     (slurp (.getErrorStream process))]
    (if (= exit-code 0)
      output
      error)))