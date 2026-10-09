#pragma once
#include <algorithm>
#include <vector>

// Re-evaluate the last prompt token: its logits must belong to this exact request.
template<class Token>
size_t reusable_prompt_prefix(const std::vector<Token> & previous, const std::vector<Token> & next) {
    if (next.empty()) return 0;
    const auto limit = std::min(previous.size(), next.size() - 1);
    size_t shared = 0;
    while (shared < limit && previous[shared] == next[shared]) ++shared;
    return shared;
}
