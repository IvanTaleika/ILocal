package ILocal.service;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BitFlagService {
    public enum StatusFlag {

        DEFAULT_WAS_CHANGED,
        FUZZY;

        private final int flag;

        StatusFlag() {
            this.flag = 1 << this.ordinal();
        }

        public int getValue() {
            return this.flag;
        }
    }

    public EnumSet<StatusFlag> getStatusFlags(int statusValue) {
        EnumSet<StatusFlag> statusFlags = EnumSet.noneOf(StatusFlag.class);
        EnumSet<StatusFlag> flags = EnumSet.allOf(StatusFlag.class);
        flags.forEach(flag -> {
                    int value = flag.getValue();
                    if ((value & statusValue) == value) {
                        statusFlags.add(flag);
                    }
                }
        );
        return statusFlags;
    }

    public int getStatusValue(Set<StatusFlag> flags) {
        int value = 0;
        for (StatusFlag statusFlag : flags) {
            value+=statusFlag.getValue();
        }
        return value;
    }

}
