package GUI.progressgui;

public interface ProgressFrame {
    void setProcess(String process);

    void setMax(int max);

    void setProgress(int progress);

    void setDone();

    void increment();
}
