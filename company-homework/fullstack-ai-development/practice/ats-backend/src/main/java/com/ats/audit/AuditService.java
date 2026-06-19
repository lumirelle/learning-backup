package com.ats.audit;

import com.ats.common.exception.BizException;
import com.ats.common.exception.ErrorCode;
import com.ats.common.security.SecurityUtil;
import com.ats.job.HrJobScope;
import com.ats.job.HrJobScopeService;
import com.ats.repository.StageLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int EXPORT_LIMIT = 10_000;

    private final StageLogMapper stageLogMapper;
    private final HrJobScopeService hrJobScopeService;

    @Transactional(readOnly = true)
    public String exportStageLogsCsv() {
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isHr()) {
            throw BizException.of(ErrorCode.FORBIDDEN);
        }
        HrJobScope scope = SecurityUtil.isAdmin() ? null : hrJobScopeService.currentScopeOrNull();
        List<Map<String, Object>> rows = stageLogMapper.exportRows(scope, EXPORT_LIMIT);

        StringBuilder sb = new StringBuilder("operated_at,application_id,job_id,job_title,from_stage,to_stage,note,operator_email\n");
        for (Map<String, Object> row : rows) {
            sb.append(csv(row.get("operated_at")))
                    .append(',').append(csv(row.get("application_id")))
                    .append(',').append(csv(row.get("job_id")))
                    .append(',').append(csv(row.get("job_title")))
                    .append(',').append(csv(row.get("from_stage")))
                    .append(',').append(csv(row.get("to_stage")))
                    .append(',').append(csv(row.get("note")))
                    .append(',').append(csv(row.get("operator_email")))
                    .append('\n');
        }
        return sb.toString();
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v).replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
