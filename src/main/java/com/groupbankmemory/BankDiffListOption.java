package com.groupbankmemory;

import com.groupbankmemory.data.BankSave;
import lombok.ToString;
import lombok.Value;

@Value
public class BankDiffListOption {
    enum Type {
        CURRENT, SNAPSHOT
    }

    String listText;
    Type bankType;
    @ToString.Exclude BankSave save;
}
