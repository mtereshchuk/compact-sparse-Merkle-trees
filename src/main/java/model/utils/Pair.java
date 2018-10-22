package model.utils;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
@RequiredArgsConstructor(staticName = "of")
public class Pair<F, S> {
    @Nullable F first;
    @Nullable S second;
}
