import http from 'k6/http';
import { sleep } from 'k6';
import { check } from 'k6';

// 목표: 순간적으로 10,000 VU를 띄워서 동시 연결 폭주 유발
export const options = {
    stages: [
        { duration: '10s', target: 10000 },   // 10초 만에 10,000 VU로 ramp-up
        { duration: '60s', target: 10000 },   // 1분 동안 10,000 유지
        { duration: '10s', target: 0 },       // ramp-down
    ],
    thresholds: {
        http_req_failed: ['rate<0.3'],        // 실패율 30% 미만 기대
        http_req_duration: ['p(95)<3000'],   // 95% 요청이 5초 이내
    },
};

export default function () {
    const res = http.get('http://localhost:8080/test');

    check(res, {
        'status is 200': (r) => r.status === 200,
        'no connection error': (r) => r.status !== 0,  // status 0 = 연결 실패
    });

    // VU가 너무 빨리 재시도하지 않게 약간 대기 (필요시 제거 가능)
    sleep(0.1);
}