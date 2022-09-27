# Compute Infrastructure with Logging and Analysis

Compute infrastructure is a system that provides computing powers to users. They can submit to
it a task among a specified set of tasks. Each submitted task will be identified inside the system
with a Universally Unique Identifier (UUID).
The system has a pool of available processes and each submitted task will be executed onto a single
process taken from the pool. If the number of submitted tasks exceeds the number of available
processes, exceeding tasks will be put into a waiting queue with FIFO policy and priority set to
normal.
In case a process fails during the execution, it is resubmitted into the waiting queue in a silent way
(i.e. no notification of the failure is sent to the user) and its priority is increased to high. Tasks
can be submitted to the system using a web interface.
Each request from a client includes the name of the task to be executed, a payload with the
parameters to be used in the computation and the name of a directory where to store results.
Users will receive a notification when the task they submitted has completed the execution and
the result has been successfully stored to the disk.
The system logs information about task life-cycle, analyzing periodically statistics and storing
results in a database.
