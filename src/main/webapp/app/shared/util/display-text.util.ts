const genreMatchers: Array<{ patterns: RegExp[]; label: string }> = [
  { patterns: [/hanh dong/i, /action/i], label: 'Hành động' },
  { patterns: [/tinh cam/i, /romance/i], label: 'Tình cảm' },
  { patterns: [/hoat hinh/i, /animation/i], label: 'Hoạt hình' },
  { patterns: [/phieu luu/i, /adventure/i], label: 'Phiêu lưu' },
  { patterns: [/khoa hoc vien tuong/i, /sci/i], label: 'Khoa học viễn tưởng' },
  { patterns: [/kinh di/i, /horror/i], label: 'Kinh dị' },
  { patterns: [/hai/i, /comedy/i], label: 'Hài' },
  { patterns: [/chinh kich/i, /drama/i], label: 'Chính kịch' },
  { patterns: [/gia dinh/i, /family/i], label: 'Gia đình' },
  { patterns: [/than thoai/i, /fantasy/i], label: 'Thần thoại' },
  { patterns: [/trinh tham/i, /detective/i, /mystery/i], label: 'Trinh thám' },
  { patterns: [/tam ly/i, /psychological/i], label: 'Tâm lý' },
  { patterns: [/am nhac/i, /music/i], label: 'Âm nhạc' },
];

function toTitleCase(value: string): string {
  return value.replace(/\b\p{L}/gu, char => char.toUpperCase());
}

export function repairVietnameseText(value?: string | null): string {
  return (value ?? '').trim();
}

export function normalizeForMatch(value?: string | null): string {
  return repairVietnameseText(value)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .replace(/[^a-zA-Z0-9]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
}

export function formatGenreLabel(value?: string | null): string {
  const source = repairVietnameseText(value);
  if (!source) {
    return 'Chưa cập nhật';
  }

  const normalized = normalizeForMatch(source);
  for (const matcher of genreMatchers) {
    if (matcher.patterns.some(pattern => pattern.test(normalized))) {
      return matcher.label;
    }
  }

  const cleaned = source.replace(/\?/g, '').replace(/\s+/g, ' ').trim();
  return cleaned ? toTitleCase(cleaned.toLowerCase()) : 'Chưa cập nhật';
}

export function formatUserStatusLabel(value?: string | null): string {
  const status = normalizeForMatch(value);
  if (status === 'active' || status === 'hoat dong') {
    return 'Hoạt động';
  }
  return 'Bị khóa';
}

export function formatRoleLabel(value?: string | null): string {
  const role = normalizeForMatch(value);
  if (role === 'role_admin' || role === 'quan tri' || role === 'quan tri vien') {
    return 'Quản trị';
  }
  if (role === 'role_user' || role === 'khach hang' || role === 'nguoi dung') {
    return 'Người dùng';
  }
  return repairVietnameseText(value).trim() || 'Chưa phân quyền';
}

export function formatTicketStatusLabel(value?: string | null): string {
  const status = normalizeForMatch(value);
  if (status === 'paid' || status === 'da thanh toan' || status === 'success' || status === 'done' || status === '2') {
    return 'Đã thanh toán';
  }
  if (status === 'cancelled' || status === 'huy' || status === '3') {
    return 'Đã hủy';
  }
  if (status === 'unpaid' || status === 'chua thanh toan' || status === 'pending' || status === '1') {
    return 'Chờ thanh toán';
  }
  return repairVietnameseText(value).trim() || 'Chưa rõ';
}
